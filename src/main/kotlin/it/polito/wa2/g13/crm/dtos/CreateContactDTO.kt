package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.contact.Address
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateContactDTO(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,
    @field:NotBlank
    @field:Size(max = 255)
    val surname: String,
    val category: ContactCategory,
    @field:Size(max = 255)
    val ssn: String?,
    @field:Valid
    val telephones: List<CreateTelephoneDTO>,
    @field:Valid
    val emails: List<CreateEmailDTO>,
    @field:Valid
    val addresses: List<CreateAddressDTO>,
) {
    companion object {
        @JvmStatic
        fun from(contact: ContactDTO): CreateContactDTO = CreateContactDTO(
            name = contact.name,
            surname = contact.surname,
            category = contact.category,
            ssn = contact.ssn,
            telephones = contact.telephones.map { CreateTelephoneDTO(it.number) },
            emails = contact.emails.map { CreateEmailDTO(it.email) },
            addresses = contact.addresses.map { CreateAddressDTO.from(it) }
        )
    }
}

data class CreateEmailDTO(
    @field:Email
    @field:NotBlank
    @field:Size(max = 255)
    val email: String,
) {
    companion object {
        @JvmStatic
        fun from(email: EmailDTO): CreateEmailDTO = CreateEmailDTO(
            email = email.email,
        )
    }
}

data class CreateTelephoneDTO(
    @field:Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$")
    val number: String,
)

data class CreateAddressDTO(
    @field:NotBlank
    @field:Size(max = 255)
    val civic: String,
    @field:NotBlank
    @field:Size(max = 255)
    val street: String,
    @field:NotBlank
    @field:Size(max = 255)
    val city: String,
    @field:NotBlank
    @field:Size(max = 255)
    val postalCode: String,
    @field:NotBlank
    @field:Size(max = 255)
    val country: String,
) {
    companion object {
        @JvmStatic
        fun from(address: Address): CreateAddressDTO = CreateAddressDTO(
            civic = address.civic,
            street = address.street,
            city = address.city,
            postalCode = address.postalCode,
            country = address.country,
        )

        @JvmStatic
        fun from(address: AddressDTO): CreateAddressDTO = CreateAddressDTO(
            civic = address.civic,
            street = address.street,
            city = address.city,
            postalCode = address.postalCode,
            country = address.country,
        )
    }
}