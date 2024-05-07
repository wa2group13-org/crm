package it.polito.wa2.g13.crm.data.joboffer

import it.polito.wa2.g13.crm.data.BaseEntity
import jakarta.persistence.*
import java.time.Duration
import java.time.OffsetDateTime

val PROFIT_MARGIN = 1.2

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
    var customer: String?, // TODO: link to real customer
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    var status: JobOfferStatus,
    @ElementCollection(fetch = FetchType.EAGER)
    var skills: MutableSet<String>,
    @Temporal(TemporalType.TIMESTAMP)
    var startTime: OffsetDateTime,
    @Temporal(TemporalType.TIMESTAMP)
    var endTime: OffsetDateTime,

    var notes: String?,


    ) : BaseEntity() {

    val duration = Duration.between(startTime, endTime)
    val value = duration.toDays() * PROFIT_MARGIN * 1 // TODO: professional_rate missing
}