package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.customer.Customer

data class CustomerDTO(
    val id: Long,
    val jobOffers: List<JobOfferDTO>,
    val note: String?,
    val contact: ContactDTO,
) {
    companion object {
        @JvmStatic
        fun from(customer: Customer): CustomerDTO = CustomerDTO(
            id = customer.id,
            note = customer.note,
            jobOffers = customer.jobOffers.map { JobOfferDTO.from(it) }.toList(),
            contact = ContactDTO.from(customer.contact)
        )
    }
}

data class CreateCustomerDTO(
    val jobOffers: List<JobOfferDTO>,
    val note: String?,
    val contact: ContactDTO
) {
    companion object {
        @JvmStatic
        fun from(customerDTO: CustomerDTO): CreateCustomerDTO = CreateCustomerDTO(
            jobOffers = customerDTO.jobOffers,
            note = customerDTO.note,
            contact = customerDTO.contact
        )
    }
}