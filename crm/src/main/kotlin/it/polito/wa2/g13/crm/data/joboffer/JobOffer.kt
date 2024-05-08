package it.polito.wa2.g13.crm.data.joboffer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.customer.Customer
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.JobOfferFilters
import it.polito.wa2.g13.crm.dtos.UpdateJobOfferDTO
import jakarta.persistence.*
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

const val PROFIT_MARGIN = 1.2

enum class JobOfferStatus {
    Created,
    SelectionPhase,
    CandidateProposal,
    Consolidated,
    Done,
    Aborted,
}

@Entity
class JobOffer(
    @ManyToOne
    var customer: Customer,
    @OneToOne
    var professional: Professional?,
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    var status: JobOfferStatus,
    @ElementCollection(fetch = FetchType.EAGER)
    var skills: MutableSet<String>,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "jobOffer")
    var notes: MutableSet<JobOfferHistory>,
    var duration: Long //duration in days
) : BaseEntity() {
    @Transient
    val value: Double? =
        professional?.let {
            duration * PROFIT_MARGIN * it.dailyRate
        }

    fun update(updatedJobOffer: UpdateJobOfferDTO) {
        this.skills = updatedJobOffer.skills.toMutableSet()
        this.status = updatedJobOffer.status
        this.description = updatedJobOffer.description
        this.duration = updatedJobOffer.duration
    }

    object Spec {
        fun withFilters(filters: JobOfferFilters?): Specification<JobOffer> {
            return Specification { root, _, criteriaBuilder ->
                val jobOffer: Root<JobOffer> = root
                val predicates = mutableListOf<Predicate>()

                if (filters == null) {
                    return@Specification criteriaBuilder.and()
                }

                if (filters.byCustomerId != null) {
                    val predicate = criteriaBuilder.equal(jobOffer.get<Long>("customer_id"), filters.byCustomerId)

                    predicates.add(predicate)
                }
                if (filters.byProfessionalId != null) {
                    val predicate =
                        criteriaBuilder.equal(jobOffer.get<Long>("professional_id"), filters.byProfessionalId)

                    predicates.add(predicate)
                }
                if (filters.byStatus != null) {

                    val predicate = jobOffer.get<JobOfferStatus>("status").`in`(filters.byStatus)

                    predicates.add(predicate)
                }

                return@Specification criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
    }
}


