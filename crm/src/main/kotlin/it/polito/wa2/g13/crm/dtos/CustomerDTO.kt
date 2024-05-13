package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.customer.Customer

data class CustomerDTO(
    val id: Long,
    val offers: List<CreateJobOfferDTO>,
    val note: String?,
    val contact: CreateContactDTO,
) {
    companion object {
        @JvmStatic
        fun from(customer: Customer): CustomerDTO = CustomerDTO(
            id = customer.id,
            note = customer.note,
            offers = customer.offers.map { CreateJobOfferDTO.from(JobOfferDTO.from(it)) }.toList(),
            contact = CreateContactDTO.from(ContactDTO.from(customer.contact))
        )
    }
}

data class CreateCustomerDTO(
    val offers: List<CreateJobOfferDTO>,
    val note: String?,
    val contact: CreateContactDTO
) {
    companion object {
        @JvmStatic
        fun from(customerDTO: CustomerDTO): CreateCustomerDTO = CreateCustomerDTO(
            offers = customerDTO.offers,
            note = customerDTO.note,
            contact = customerDTO.contact
        )
    }
}