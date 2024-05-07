package it.polito.wa2.g13.crm.data.joboffer

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.Professional
import it.polito.wa2.g13.crm.data.customer.Customer
import jakarta.persistence.*

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
    val duration: Long //duration in days

    /***
     * We decided to remove those below as the start and end time are not essential.
     * The recruiter will set the status as Done once the job is finished
     * ***/
//    @Temporal(TemporalType.TIMESTAMP)
//    var startTime: OffsetDateTime,
//    @Temporal(TemporalType.TIMESTAMP)
//    var endTime: OffsetDateTime,
) : BaseEntity() {
    val value: Double? =
        professional?.let {
            duration * PROFIT_MARGIN * it.dailyRate
        }
}