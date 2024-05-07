package it.polito.wa2.g13.crm.data.contact

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.customer.Customer
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.ContactDTO
import it.polito.wa2.g13.crm.dtos.CreateContactDTO
import jakarta.persistence.*
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

enum class ContactCategory {
    Customer,
    Professional,
    Unknown,
}

@Entity
class Contact(
    var name: String,
    var surname: String,
    @Enumerated
    var category: ContactCategory,

    var ssn: String?,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "contacts")
    var telephones: MutableSet<Telephone>,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "contacts")
    var emails: MutableSet<Email>,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "contacts")
    var addresses: MutableSet<Address>,

    @OneToOne(mappedBy = "contact")
    var customer : Customer?,


    @OneToOne(mappedBy = "contact", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var professional: Professional?

) : BaseEntity() {
    companion object {
        @JvmStatic
        fun from(contact: CreateContactDTO): Contact = Contact(
            name = contact.name,
            surname = contact.surname,
            category = contact.category,
            ssn = contact.ssn,
            telephones = mutableSetOf(),
            emails = mutableSetOf(),
            addresses = mutableSetOf(),
            customer = null,
            professional = null,
        )

        @JvmStatic
        fun from(contact: ContactDTO): Contact = Contact(
            name = contact.name,
            surname = contact.name,
            category = contact.category,
            ssn = contact.ssn,
            telephones = mutableSetOf(),
            emails = mutableSetOf(),
            addresses = mutableSetOf(),
            customer = null,
            professional = null,
        )

        @JvmStatic
        fun createAnonymous(sender: String, channel: String): Contact {
            return Contact(
                name = sender,
                surname = channel,
                category = ContactCategory.Unknown,
                ssn =null,
                telephones = mutableSetOf(),
                emails = mutableSetOf(),
                addresses = mutableSetOf(),
                professional = null,
                customer = null,
            )
        }
    }

    data class Filters(
        val byEmail: String? = null,
        val byTelephone: String? = null,
        val byName: String? = null,
    )

    object Spec {
        fun withFilters(filters: Filters): Specification<Contact> {
            return Specification { root, query, criteriaBuilder ->
                val contact: Root<Contact> = root
                val predicates = mutableListOf<Predicate>()

                // Filter by telephone
                if (filters.byTelephone != null) {
                    val telephoneSubquery = query.subquery(Telephone::class.java)
                    val telephone = telephoneSubquery.from(Telephone::class.java)
                    val telephoneContacts: Expression<Collection<Contact>> = telephone.get("contacts")

                    telephoneSubquery.select(telephone)

                    telephoneSubquery.where(
                        criteriaBuilder.equal(telephone.get<String>("number"), filters.byTelephone),
                        criteriaBuilder.isMember(contact, telephoneContacts)
                    )

                    predicates.add(criteriaBuilder.exists(telephoneSubquery))
                }

                // Filter by email
                if (filters.byEmail != null) {
                    val emailSubquery = query.subquery(Email::class.java)
                    val email = emailSubquery.from(Email::class.java)
                    val emailContacts: Expression<Collection<Email>> = email.get("contacts")

                    emailSubquery.select(email)

                    emailSubquery.where(
                        criteriaBuilder.equal(email.get<String>("email"), filters.byEmail),
                        criteriaBuilder.isMember(contact, emailContacts)
                    )

                    predicates.add(criteriaBuilder.exists(emailSubquery))
                }

                // Filter by name
                if (filters.byName != null) {
                    val predicate = criteriaBuilder.equal(contact.get<String>("name"), filters.byName)

                    predicates.add(predicate)
                }

                return@Specification criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }

    /**
     * Update this entity using another entity of the same type.
     */
    fun update(other: Contact) {
        this.name = other.name
        this.surname = other.surname
        this.category = other.category
        this.ssn = other.ssn
    }

    fun isAnonymous(): Boolean {
        return this.category == ContactCategory.Unknown
    }
}
