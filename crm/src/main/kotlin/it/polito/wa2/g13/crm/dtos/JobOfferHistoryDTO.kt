package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import java.time.OffsetDateTime

data class JobOfferHistoryDTO(
    val id: Long,
    val assignedProfessional: Long?,
    val logTime: OffsetDateTime,
    val currentStatus: JobOfferStatus,
    val note: String?,
) {
    companion object {
        @JvmStatic
        fun from(jobOfferHistory: JobOfferHistory): JobOfferHistoryDTO {
            return JobOfferHistoryDTO(
                jobOfferHistory.id,
                jobOfferHistory.assignedProfessional?.id,
                jobOfferHistory.logTime,
                jobOfferHistory.currentStatus,
                jobOfferHistory.note,
            )
        }
    }
}

data class AddJobOfferHistoryDTO(
    val assignedProfessional: Long?,
    val currentStatus: JobOfferStatus,
    val note: String?,
)


