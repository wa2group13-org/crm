package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.contact.*

data class ContactDTO(
    val id: Long,
    val name: String,
    val surname: String,
    val category: ContactCategory,
    val ssn: String?,
    val telephones: List<TelephoneDTO>,
    val emails: List<EmailDTO>,
    val addresses: List<AddressDTO>,
) {
    companion object {
        @JvmStatic
        fun from(contact: Contact): ContactDTO = ContactDTO(
            id = contact.id,
            name = contact.name,
            surname = contact.surname,
            category = contact.category,
            ssn = contact.ssn,
            telephones = contact.telephones.map { TelephoneDTO(it.id, it.number) }.toList(),
            emails = contact.emails.map { EmailDTO(it.id, it.email) }.toList(),
            addresses = contact.addresses.map {
                AddressDTO(
                    id = it.id,
                    civic = it.civic,
                    street = it.street,
                    city = it.city,
                    postalCode = it.postalCode,
                    country = it.country,
                )
            }.toList(),
        )
    }
}

data class TelephoneDTO(
    val id: Long,
    val number: String,
) {
    companion object {
        @JvmStatic
        fun from(telephone: Telephone): TelephoneDTO = TelephoneDTO(
            id = telephone.id,
            number = telephone.number,
        )
    }
}

data class EmailDTO(
    val id: Long,
    val email: String,
) {
    companion object {
        @JvmStatic
        fun from(email: Email): EmailDTO = EmailDTO(
            id = email.id,
            email = email.email,
        )
    }
}

data class AddressDTO(
    val id: Long,
    val civic: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
) {
    companion object {
        @JvmStatic
        fun from(address: Address): AddressDTO = AddressDTO(
            id = address.id,
            civic = address.civic,
            street = address.street,
            city = address.city,
            postalCode = address.postalCode,
            country = address.country,
        )
    }
}