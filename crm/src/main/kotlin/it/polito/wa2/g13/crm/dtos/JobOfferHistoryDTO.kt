package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

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
                jobOfferHistory.logTime.truncatedTo(ChronoUnit.MICROS),
                jobOfferHistory.currentStatus,
                jobOfferHistory.note,
            )
        }
    }
}

data class CreateJobOfferHistoryNoteDTO(
    @field:NotBlank(message = "Note is required")
    @field:Size(max = 5000, message = "Note must at most 5000 characters")
    val note: String,
)


