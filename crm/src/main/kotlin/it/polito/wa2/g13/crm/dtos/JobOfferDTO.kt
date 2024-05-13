package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

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

data class CreateSkillsDTO(
    @field:Size(min = 0, max = 255)
    @field:NotBlank
    val skill: String
)

data class CreateJobOfferDTO(
    @field:Min(0, message = "customerId cannot be negative")
    val customerId: Long,
    @field:NotBlank(message = "description should not be blank")
   @field:Size(min = 0, max = 255) val description: String,
    val status: JobOfferStatus,
    @field:Size(min = 0, max = 100) val skills: Set<CreateSkillsDTO>,
    @field:Min(0, message = "duration cannot be negative")
    val duration: Long
)

data class UpdateJobOfferDetailsDTO(
    @field:NotBlank(message = "description should not be blank")
    val description: String,
    @field:Size(min = 0, max = 100) val skills: Set<CreateSkillsDTO>,
    @field:Min(0, message = "duration cannot be negative") val duration: Long,
)

data class UpdateJobOfferStatusDTO(
    val status: JobOfferStatus,
    @field:Min(0, message = "professional id cannot be negative")
    val professionalId: Long?,
    @field:Size(min = 0, max = 1000)
    @field:NotBlank(message = "note cannot be null")
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