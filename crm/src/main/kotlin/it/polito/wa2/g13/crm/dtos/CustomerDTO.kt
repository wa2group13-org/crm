package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.customer.Customer
import it.polito.wa2.g13.crm.data.joboffer.JobOffer

class JobOfferDTO{
    companion object{
        @JvmStatic
        fun from(job : JobOffer) = JobOfferDTO()
    }
}

class CustomerDTO(
    val id: Long,
    val offers: List<JobOfferDTO>,
    val note: String?,
    val contact: ContactDTO,
)
{
    companion object {
        @JvmStatic
        fun from(customer: Customer): CustomerDTO = CustomerDTO(
            id = customer.id,
            note = customer.note,
            offers = customer.offers.map { JobOfferDTO.from(it) }.toList(),
            contact = ContactDTO.from(customer.contact)
        )
    }
}