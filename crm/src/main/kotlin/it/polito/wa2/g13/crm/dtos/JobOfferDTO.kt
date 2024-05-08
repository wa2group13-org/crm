package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus

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
    val customerId: Long,
    val description: String,
    val status: JobOfferStatus,
    val skills: List<String>,
    val duration: Long,
)

data class UpdateJobOfferDTO(
    val description: String,
    val status: JobOfferStatus,
    val skills: List<String>,
    val duration: Long,
)

data class JobOfferFilters(
    val byCustomerId: Long? = null,
    val byProfessionalId: Long? = null,
    val byStatus: Set<JobOfferStatus>?
)

data class GetJobOffers(
    val filters: JobOfferFilters?,
    val page: Int,
    val limit: Int
)