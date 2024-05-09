package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class JobOfferDTO(
    val id: Long,
    val customerId: Long,
    val professionalId: Long?,
    val description: String?,
    val status: JobOfferStatus,
    val skills: List<String>,
    val duration: Long,
    val notes: List<JobOfferHistoryDTO>,
    val value: Double?
) {
    companion object {
        @JvmStatic
        fun from(jobOffer: JobOffer): JobOfferDTO {
            return JobOfferDTO(
                jobOffer.id,
                jobOffer.customer.id,
                jobOffer.professional?.id,
                jobOffer.description,
                jobOffer.status,
                jobOffer.skills.toList(),
                jobOffer.duration,
                jobOffer.notes.map { JobOfferHistoryDTO.from(it) },
                jobOffer.value
            )
        }
    }
}

data class CreateJobOfferDTO(
    @field:Min(0, message = "customerId cannot be negative") val customerId: Long,
    @field:NotBlank(message = "description should not be blank") val description: String,
    val status: JobOfferStatus,
    val skills: List<String>,
    @field:Min(0, message = "duration cannot be negative") val duration: Long
)

data class UpdateJobOfferDetailsDTO(
    @field:NotBlank(message = "description should not be blank")
    val description: String,
    val skills: List<String>,
    @field:Min(0, message = "duration cannot be negative") val duration: Long,
)

data class UpdateJobOfferStatusDTO(
    val status: JobOfferStatus,
    @field:Min(0, message = "professional id cannot be negative")
    val professionalId: Long?,
    val note: String?
)

data class JobOfferFilters(
    @field:Min(0, message = "customerId cannot be negative") val byCustomerId: Long? = null,
    @field:Min(0, message = "professionalId cannot be negative") val byProfessionalId: Long? = null,
    val byStatus: Set<JobOfferStatus>?
)

data class GetJobOffers(
    @field:Valid val filters: JobOfferFilters?,
    @field:Min(0, message = "page cannot be negative")
    val page: Int,
    @field:Min(0, message = "limit cannot be negative")
    @field:Max(200, message = "limit cannot be negative")
    val limit: Int
)