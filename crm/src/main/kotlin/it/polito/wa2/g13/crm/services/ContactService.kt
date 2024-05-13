package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.ContactException
import org.springframework.data.domain.Page

interface ContactService {

    /**
     * Returns a list of [ContactDTO] by using pagination and filters.
     */
    fun getContacts(
        page: Int,
        limit: Int,
        byEmail: String?,
        byTelephone: String?,
        byName: String?,
    ): Page<ContactDTO>

    /**
     * Get a contact by [id].
     *
     * @throws ContactException.NotFound if no contact was found
     */
    @Throws(ContactException.NotFound::class)
    fun getContactById(id: Long): ContactDTO

    /**
     * Creates a new [it.polito.wa2.g13.crm.data.contact.Contact] in the database
     *
     * @return return the id of the new [it.polito.wa2.g13.crm.data.contact.Contact]
     */
    fun createContact(contactDto: CreateContactDTO): Long

    /**
     * Delete a contact by [id].
     *
     * @throws ContactException.NotFound if no contact was found
     */
    @Throws(ContactException.NotFound::class)
    fun deleteContactById(id: Long)

    @Throws(ContactException.NotFound::class)
    fun createContactEmail(contactId: Long, emailDto: CreateEmailDTO): Long

    @Throws(ContactException.NotFound::class)
    fun getContactEmailById(contactId: Long, emailId: Long): EmailDTO

    @Throws(ContactException.NotFound::class)
    fun getContactEmails(contactId: Long): List<EmailDTO>

    @Throws(ContactException.NotFound::class)
    fun updateContactEmail(contactId: Long, emailId: Long, emailDto: CreateEmailDTO): Long?

    @Throws(ContactException.NotFound::class)
    fun deleteContactEmailById(contactId: Long, emailId: Long)

    @Throws(ContactException.NotFound::class)
    fun getContactTelephoneById(contactId: Long, telephoneId: Long): TelephoneDTO

    @Throws(ContactException.NotFound::class)
    fun createContactTelephone(contactId: Long, telephoneDto: CreateTelephoneDTO): Long

    @Throws(ContactException.NotFound::class)
    fun deleteContactTelephoneById(contactId: Long, telephoneId: Long)

    @Throws(ContactException.NotFound::class)
    fun getContactTelephones(contactId: Long): List<TelephoneDTO>

    @Throws(ContactException.NotFound::class)
    fun updateContactTelephone(contactId: Long, telephoneId: Long, telephoneDto: CreateTelephoneDTO): Long?

    @Throws(ContactException.NotFound::class)
    fun createContactAddress(contactId: Long, addressDto: CreateAddressDTO): Long

    @Throws(ContactException.NotFound::class)
    fun getContactAddressById(contactId: Long, addressId: Long): AddressDTO

    @Throws(ContactException.NotFound::class)
    fun getContactAddresses(contactId: Long): List<AddressDTO>

    @Throws(ContactException.NotFound::class)
    fun updateContactAddress(contactId: Long, addressId: Long, addressDto: CreateAddressDTO): Long?

    @Throws(ContactException.NotFound::class)
    fun deleteContactAddressById(contactId: Long, addressId: Long)

    @Throws(ContactException.NotFound::class)
    fun updateContact(contactId: Long, contactDto: CreateContactDTO): Long?
}