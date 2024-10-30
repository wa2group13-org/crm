package it.polito.wa2.g13.crm.data.customer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Address
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.CreateCustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerFilters
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

@Entity
class Customer(

    @OneToOne(
        cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH],
        fetch = FetchType.EAGER
    )
    var contact: Contact,

    @OneToMany(mappedBy = "customer")
    var jobOffers: MutableSet<JobOffer>,

    var note: String?,


    ) : BaseEntity() {
    companion object {
        fun from(customer: CreateCustomerDTO, contact: Contact): Customer = Customer(
            note = customer.note,
            jobOffers = mutableSetOf(),
            contact = contact,
        )
    }

    object Spec {
        fun withFilters(filters: CustomerFilters): Specification<Customer> =
            Specification { root, query, criteriaBuilder ->
                val customer: Root<Customer> = root
                val predicates: MutableList<Predicate> = mutableListOf()

                // Filter by fullName
                if (filters.byFullName != null) {
                    val contact = customer.get<Contact>("contact")

                    val predicate = customer.join<Professional, Contact>("contact").on(
                        criteriaBuilder.like(
                            criteriaBuilder.lower(
                                criteriaBuilder.concat(
                                    criteriaBuilder.concat(contact.get("name"), criteriaBuilder.literal(" ")),
                                    contact.get("surname")
                                )
                            ), "%${filters.byFullName.lowercase()}%"
                        )
                    ).on

                    predicates.add(predicate)
                }

                // Filter by location
                if (filters.byLocation != null) {
                    val addressSubquery = query.subquery(Address::class.java)
                    val address = addressSubquery.from(Address::class.java)
                    val addressContact: Expression<Collection<Contact>> = address.get("contacts")

                    val professionalContact = customer.get<Contact>("contact")

                    val filterPredicates: MutableList<Predicate> = mutableListOf()

                    val addFilter = { filter: String, name: String ->
                        filterPredicates.add(
                            criteriaBuilder.like(criteriaBuilder.lower(address.get(name)), "%${filter.lowercase()}%")
                        )
                    }

                    filters.byLocation.byCivic?.let {
                        addFilter(it, "civic")
                    }

                    filters.byLocation.byStreet?.let {
                        addFilter(it, "street")
                    }

                    filters.byLocation.byCity?.let {
                        addFilter(it, "city")
                    }

                    filters.byLocation.byPostalCode?.let {
                        addFilter(it, "postalCode")
                    }

                    filters.byLocation.byCountry?.let {
                        addFilter(it, "country")
                    }

                    addressSubquery.select(address)

                    addressSubquery.where(
                        *filterPredicates.toTypedArray(),
                        criteriaBuilder.isMember(professionalContact, addressContact),
                    )

                    val predicate = criteriaBuilder.exists(addressSubquery)
                    predicates.add(predicate)
                }

                return@Specification criteriaBuilder.and(*predicates.toTypedArray())
            }
    }
}
