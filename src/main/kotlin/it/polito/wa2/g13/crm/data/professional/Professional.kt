package it.polito.wa2.g13.crm.data.professional

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Address
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import jakarta.persistence.*
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

enum class EmploymentState {
    Employed,
    Available,
    NotAvailable,
}

@Entity
class Professional(
    @Enumerated
    var employmentState: EmploymentState,

    @OneToOne(mappedBy = "professional", fetch = FetchType.LAZY)
    var jobOffer: JobOffer?,

    @OneToMany(mappedBy = "assignedProfessional", fetch = FetchType.LAZY)
    var jobOfferHistory: MutableSet<JobOfferHistory>,

    var dailyRate: Double,

    @ElementCollection(fetch = FetchType.EAGER)
    var skills: MutableSet<String>,

    @Column(columnDefinition = "text")
    var notes: String?,

    @OneToOne(
        cascade = [CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH],
        fetch = FetchType.EAGER
    )
    @JoinColumn(referencedColumnName = "id", name = "contact_id", foreignKey = ForeignKey())
    var contact: Contact,
) : BaseEntity() {
    companion object {
        @JvmStatic
        fun from(professional: CreateProfessionalDTO, contact: Contact): Professional = Professional(
            employmentState = professional.employmentState,
            dailyRate = professional.dailyRate,
            skills = professional.skills.map { it.skill }.toMutableSet(),
            notes = professional.notes,
            contact = contact,
            jobOffer = null,
            jobOfferHistory = mutableSetOf(),
        ).apply {
            contact.category = ContactCategory.Professional
            contact.professional = this
        }
    }

    object Spec {
        fun withFilters(filters: ProfessionalFilters): Specification<Professional> {
            return Specification { root, query, criteriaBuilder ->
                val professional: Root<Professional> = root
                val predicates: MutableList<Predicate> = mutableListOf()

                // Filter by skills
                if (filters.bySkills != null) {
                    val predicate = professional.join<Professional, String>("skills").`in`(filters.bySkills)
                    predicates.add(predicate)
                }

                // Filter by employmentState
                if (filters.byEmploymentState != null) {
                    val predicate = criteriaBuilder.equal(
                        professional.get<EmploymentState>("employmentState"),
                        filters.byEmploymentState
                    )
                    predicates.add(predicate)
                }

                // Filter by location
                if (filters.byLocation != null) {
                    val addressSubquery = query.subquery(Address::class.java)
                    val address = addressSubquery.from(Address::class.java)
                    val addressContact: Expression<Collection<Contact>> = address.get("contacts")

                    val professionalContact = professional.get<Contact>("contact")

                    val filterPredicates: MutableList<Predicate> = mutableListOf()

                    val addFilter = { filter: String, name: String ->
                        filterPredicates.add(
                            criteriaBuilder.equal(address.get<String>(name), filter)
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

    fun update(professional: Professional) {
        employmentState = professional.employmentState
        dailyRate = professional.dailyRate
        skills = professional.skills
        notes = professional.notes
        contact = professional.contact.apply { this.professional = this@Professional }
        jobOffer = professional.jobOffer?.apply { this.professional = this@Professional }
        jobOfferHistory = professional.jobOfferHistory.onEach { it.assignedProfessional = this@Professional }
    }
}