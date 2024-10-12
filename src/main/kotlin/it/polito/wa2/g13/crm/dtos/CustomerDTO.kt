package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.customer.Customer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

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
    @field:Size(min = 1, max = 5000)
    @field:NotBlank
    val note: String?,
    val contactId: Long,
    @field:Valid
    val contactInfo: CreateContactDTO?
) {
    companion object {
        @JvmStatic
        fun from(customerDTO: CustomerDTO): CreateCustomerDTO = CreateCustomerDTO(
            note = customerDTO.note,
            contactId = customerDTO.contact.id,
            contactInfo = null,
        )

        @JvmStatic
        fun from(contactId: Long): CreateCustomerDTO = CreateCustomerDTO(
            note = null,
            contactId = contactId,
            contactInfo = null,
        )
    }
}