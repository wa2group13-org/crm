package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Address
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.contact.Email
import it.polito.wa2.g13.crm.data.contact.Telephone
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.repositories.AddressRepository
import it.polito.wa2.g13.crm.repositories.ContactRepository
import it.polito.wa2.g13.crm.repositories.EmailRepository
import it.polito.wa2.g13.crm.repositories.TelephoneRepository
import it.polito.wa2.g13.crm.utils.nullable
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.Throws

@Service
@Transactional
class ContactServiceImpl(
    private val contactRepository: ContactRepository,
    private val emailRepository: EmailRepository,
    private val telephoneRepository: TelephoneRepository,
    private val addressRepository: AddressRepository,
) : ContactService {
    companion object {
        private val logger = LoggerFactory.getLogger(ContactServiceImpl::class.java)
    }

    private fun createContactEntity(contactDto: CreateContactDTO): Contact {
        val contact = Contact.from(contactDto)

        // Find all related entities
        val telephones = telephoneRepository.findAllByNumbers(contactDto.telephones.map { it.number }).toMutableSet()
        val emails = emailRepository.findAllByEmails(contactDto.emails.map { it.email }).toMutableSet()
        val addresses = contactDto.addresses.mapNotNull {
            addressRepository.findByAddress(
                city = it.city,
                civic = it.civic,
                street = it.street,
                postalCode = it.postalCode
            )
        }.toMutableSet()

        // Convert them to equivalent part of the input DTO
        val telephoneNumbers = telephones.map { it.number }.toSet()
        val emailEmails = emails.map { it.email }.toSet()
        val addressStrings = addresses.map { CreateAddressDTO.from(it) }.toMutableSet()

        // Create the entities that don't exist
        telephones.addAll(contactDto.telephones.filter { it.number !in telephoneNumbers }.map { Telephone.from(it) })
        emails.addAll(contactDto.emails.filter { it.email !in emailEmails }.map { Email.from(it) })
        addresses.addAll(contactDto.addresses.filter { it !in addressStrings }.map { Address.from(it) })

        // Add the contact to all the related entities
        contact.telephones = telephones.onEach { it.contacts.add(contact) }
        contact.emails = emails.onEach { it.contacts.add(contact) }
        contact.addresses = addresses.onEach { it.contacts.add(contact) }

        return contact
    }

    /**
     * Handled the update of the entities linked to the [Contact] entity.
     *
     * Updating a related entity is impossible due to the [jakarta.persistence.ManyToMany] relationships
     * between them, the most optimal solution is to delete the entity from the contact first, and then
     * create a new one or link an existing one if it exists.
     */
    private inline fun <reified T : BaseEntity> manageManyToManyUpdate(
        entityFromId: T?,
        entityWithUniqueConstraint: T?,
        contactId: Long,
        newEntity: () -> T,
        getContactEntityCollection: (Contact) -> MutableSet<T>,
        getEntityContacts: (T) -> MutableSet<Contact>,
        repository: JpaRepository<T, Long>
    ): Long? {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        val entities = getContactEntityCollection(contact)

        // We are trying to update an entity to the same value
        if (entityFromId != null && entityFromId == entityWithUniqueConstraint)
            return null

        // There are 4 possible cases:
        val returnId = when {
            // Create new entity
            entityFromId == null && entityWithUniqueConstraint == null -> {
                val createdEntity = newEntity()
                getEntityContacts(createdEntity).add(contact)
                repository.save(createdEntity).let {
                    contactRepository.save(contact)

                    logger.info("Created new ${T::class.simpleName}@${it.id} of Contact@$contactId when it did not exist.")
                    it.id
                }
            }

            // Link existing entity with the same unique constraints
            entityFromId == null && entityWithUniqueConstraint != null -> {
                entities.add(entityWithUniqueConstraint)
                getEntityContacts(entityWithUniqueConstraint).add(contact)
                repository.save(entityWithUniqueConstraint).let {
                    contactRepository.save(contact)

                    logger.info("Linked ${T::class.simpleName}@${it.id} to Contact@$contactId when the same email already existed.")
                    it.id
                }
            }

            // Remove the current entity and create a new one
            entityFromId != null && entityWithUniqueConstraint == null -> {
                entities.remove(entityFromId)
                getEntityContacts(entityFromId).remove(contact)
                repository.save(entityFromId)
                logger.info("Removed ${T::class.simpleName}@${entityFromId.id} of Contact@$contactId to create a new one.")

                val createdEntity = repository.save(newEntity())
                getEntityContacts(createdEntity).add(contact)
                repository.save(createdEntity).let {
                    contactRepository.save(contact)

                    logger.info("Created new ${T::class.simpleName}@${it.id} of Contact@$contactId .")
                    it.id
                }
            }


            // Remove current email and link the already existing one
            entityFromId != null && entityWithUniqueConstraint != null -> {
                entities.remove(entityFromId)
                getEntityContacts(entityFromId).remove(contact)
                repository.save(entityFromId).let {
                    logger.info("Removed ${T::class.simpleName}@${it.id} of Contact@$contactId to link an existing one.")
                }

                entities.add(entityWithUniqueConstraint)
                getEntityContacts(entityWithUniqueConstraint).add(contact)
                repository.save(entityWithUniqueConstraint).let {
                    contactRepository.save(contact)

                    logger.info("Linked ${T::class.simpleName}@${it.id} to Contact@$contactId after removal.")
                    it.id
                }
            }

            else -> {
                logger.error("This when should not reach this point! entityFromId: $entityFromId, entityWithUniqueConstraint: $entityWithUniqueConstraint")
                throw IllegalStateException("This when should not reach this point! entityFromId: $entityFromId, entityWithUniqueConstraint: $entityWithUniqueConstraint")
            }
        }

        return returnId
    }

    @Throws(ContactException.NotFound::class)
    private inline fun <reified T : BaseEntity> handleRelationCreation(
        contactId: Long,
        entityToCreate: T,
        entityWithUniqueConstraint: T?,
        getContactEntityCollection: (Contact) -> MutableSet<T>,
        getEntityContacts: (T) -> MutableSet<Contact>,
        repository: JpaRepository<T, Long>,
    ): Long {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        val entities = getContactEntityCollection(contact)

        if (entities.contains(entityWithUniqueConstraint)) {
            throw ContactException.Duplicate.fromRelation<T>(contactId)
        }

        // If there is already an entity with the same unique constraints link that one
        // instead of creating a new one
        return if (entityWithUniqueConstraint != null) {
            entities.add(entityWithUniqueConstraint)
            getEntityContacts(entityWithUniqueConstraint).add(contact)
            contactRepository.save(contact)

            logger.info("handleRelationCreation: Linked ${T::class.simpleName}@${entityWithUniqueConstraint.id} to Contact@$contactId")

            entityWithUniqueConstraint.id
        } else {
            val createdEntity = repository.save(entityToCreate)
            getEntityContacts(createdEntity).add(contact)
            entities.add(createdEntity)
            contactRepository.save(contact)

            logger.info("handleRelationCreation: Created new ${T::class.simpleName}@${createdEntity.id} of Contact@$contactId")

            createdEntity.id
        }
    }

    @Throws(ContactException.NotFound::class)
    private inline fun <reified T : BaseEntity> handleRelationGet(
        contactId: Long,
        entityId: Long,
        getContactEntityCollection: (Contact) -> MutableSet<T>,
    ): T {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        val entities = getContactEntityCollection(contact)

        val index = entities.indexOf(BaseEntity(entityId))
        if (index == -1)
            throw ContactException.NotFound.fromRelation<T>(contactId, entityId)

        return entities.elementAt(index)
    }

    private inline fun <reified T : BaseEntity> handleRelationDeletion(
        contactId: Long,
        entityId: Long,
        getContactEntityCollection: (Contact) -> MutableSet<T>,
        getEntityContacts: (T) -> MutableSet<Contact>,
    ) {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        val entities = getContactEntityCollection(contact)

        val index = entities.indexOf(BaseEntity(entityId))
        if (index == -1)
            throw ContactException.NotFound.fromRelation<T>(contactId, entityId)

        getEntityContacts(entities.elementAt(index)).remove(contact)
        entities.remove(entities.elementAt(index))

        contactRepository.save(contact)

        logger.info("handleRelationDeletion: Deleted ${T::class.simpleName}@${entityId} of Contact@$contactId")
    }

    override fun getContacts(
        page: Int,
        limit: Int,
        byEmail: String?,
        byTelephone: String?,
        byName: String?,
    ): List<ContactDTO> {
        return contactRepository
            .findAll(
                Contact.Spec.withFilters(
                    Contact.Filters(byEmail = byEmail, byTelephone = byTelephone, byName = byName)
                ),
                PageRequest.of(page, limit),
            )
            .map { ContactDTO.from(it) }
            .toList()
    }

    override fun getContactById(id: Long): ContactDTO {
        return contactRepository
            .findById(id)
            .map { ContactDTO.from(it) }
            .nullable()
            ?: throw ContactException.NotFound.from(id)
    }

    override fun createContact(contactDto: CreateContactDTO): Long {
        val contact = createContactEntity(contactDto)

        val saveContact = contactRepository.save(contact)

        logger.info("Created Contact with id: ${saveContact.id}")

        return saveContact.id
    }

    override fun deleteContactById(id: Long) {
        if (!contactRepository.existsById(id))
            throw ContactException.NotFound.from(id)

        contactRepository.deleteById(id)

        logger.info("deleteContactById: Deleted Contact with id: $id")
    }

    override fun createContactEmail(contactId: Long, emailDto: CreateEmailDTO): Long {
        val email = Email.from(emailDto)
        val sameEmail = emailRepository.findByEmail(emailDto.email)

        return handleRelationCreation(
            contactId = contactId,
            entityToCreate = email,
            entityWithUniqueConstraint = sameEmail,
            getContactEntityCollection = { it.emails },
            getEntityContacts = { it.contacts },
            repository = emailRepository,
        )
    }

    override fun getContactEmailById(contactId: Long, emailId: Long): EmailDTO {
        return EmailDTO.from(handleRelationGet(
            contactId = contactId,
            entityId = emailId,
            getContactEntityCollection = { it.emails }
        ))
    }

    override fun getContactEmails(contactId: Long): List<EmailDTO> {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        return contact
            .emails
            .map { EmailDTO.from(it) }
    }

    override fun updateContactEmail(contactId: Long, emailId: Long, emailDto: CreateEmailDTO): Long? {
        val email = emailRepository.findByIdAndContactId(emailId, contactId)
        val sameEmail = emailRepository.findByEmail(emailDto.email)

        return manageManyToManyUpdate(
            entityFromId = email,
            entityWithUniqueConstraint = sameEmail,
            contactId = contactId,
            newEntity = { Email.from(emailDto) },
            getContactEntityCollection = { it.emails },
            getEntityContacts = { it.contacts },
            repository = emailRepository,
        )
    }

    override fun deleteContactEmailById(contactId: Long, emailId: Long) {
        handleRelationDeletion(
            contactId = contactId,
            entityId = emailId,
            getContactEntityCollection = { it.emails },
            getEntityContacts = { it.contacts }
        )
    }

    override fun getContactTelephoneById(contactId: Long, telephoneId: Long): TelephoneDTO {
        return TelephoneDTO.from(handleRelationGet(
            contactId = contactId,
            entityId = telephoneId,
            getContactEntityCollection = { it.telephones }
        ))
    }

    override fun createContactTelephone(contactId: Long, telephoneDto: CreateTelephoneDTO): Long {
        val telephone = Telephone.from(telephoneDto)
        val sameTelephone = telephoneRepository.findByNumber(telephoneDto.number)

        return handleRelationCreation(
            contactId = contactId,
            entityToCreate = telephone,
            entityWithUniqueConstraint = sameTelephone,
            getContactEntityCollection = { it.telephones },
            getEntityContacts = { it.contacts },
            repository = telephoneRepository,
        )
    }

    override fun deleteContactTelephoneById(contactId: Long, telephoneId: Long) {
        handleRelationDeletion(
            contactId = contactId,
            entityId = telephoneId,
            getContactEntityCollection = { it.telephones },
            getEntityContacts = { it.contacts },
        )
    }

    override fun getContactTelephones(contactId: Long): List<TelephoneDTO> {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        return contact
            .telephones
            .map { TelephoneDTO.from(it) }
    }

    override fun updateContactTelephone(contactId: Long, telephoneId: Long, telephoneDto: CreateTelephoneDTO): Long? {
        val telephone = telephoneRepository.findByIdAndContactId(telephoneId, contactId)
        val sameTelephone = telephoneRepository.findByNumber(telephoneDto.number)

        return manageManyToManyUpdate(
            entityFromId = telephone,
            entityWithUniqueConstraint = sameTelephone,
            contactId = contactId,
            newEntity = { Telephone.from(telephoneDto) },
            getContactEntityCollection = { it.telephones },
            getEntityContacts = { it.contacts },
            repository = telephoneRepository,
        )
    }

    override fun createContactAddress(contactId: Long, addressDto: CreateAddressDTO): Long {
        val address = Address.from(addressDto)
        val sameAddress = addressRepository.findByAddress(
            civic = addressDto.civic,
            street = addressDto.street,
            city = addressDto.city,
            postalCode = addressDto.postalCode,
        )

        return handleRelationCreation(
            contactId = contactId,
            entityToCreate = address,
            entityWithUniqueConstraint = sameAddress,
            getContactEntityCollection = { it.addresses },
            getEntityContacts = { it.contacts },
            repository = addressRepository,
        )
    }

    override fun getContactAddressById(contactId: Long, addressId: Long): AddressDTO {
        return AddressDTO.from(handleRelationGet(
            contactId = contactId,
            entityId = addressId,
            getContactEntityCollection = { it.addresses }
        ))
    }

    override fun getContactAddresses(contactId: Long): List<AddressDTO> {
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        return contact
            .addresses
            .map { AddressDTO.from(it) }
    }

    override fun updateContactAddress(contactId: Long, addressId: Long, addressDto: CreateAddressDTO): Long? {
        val address = addressRepository.findByIdAndContactId(addressId, contactId)
        val sameAddress = addressRepository.findByAddress(
            civic = addressDto.civic,
            street = addressDto.street,
            city = addressDto.city,
            postalCode = addressDto.postalCode,
        )

        return manageManyToManyUpdate(
            entityFromId = address,
            entityWithUniqueConstraint = sameAddress,
            contactId = contactId,
            newEntity = { Address.from(addressDto) },
            getContactEntityCollection = { it.addresses },
            getEntityContacts = { it.contacts },
            repository = addressRepository,
        )
    }

    override fun deleteContactAddressById(contactId: Long, addressId: Long) {
        handleRelationDeletion(
            contactId = contactId,
            entityId = addressId,
            getContactEntityCollection = { it.addresses },
            getEntityContacts = { it.contacts },
        )
    }

    override fun updateContact(contactId: Long, contactDto: CreateContactDTO): Long? {
        val contact = contactRepository.findById(contactId).nullable()

        // Delete a contact if it exists and then replace it
        if (contact != null) {
            contactRepository.delete(contact)
            logger.info("updateContact: Deleted Contact@$contactId")
        }

        val newContact = createContactEntity(contactDto)

        return contactRepository.save(newContact).let {
            logger.info("updateContact: Create Contact@$contactId")
            it.id
        }
    }
}