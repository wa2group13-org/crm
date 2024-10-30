package it.polito.wa2.g13.crm.data.contact

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.dtos.AddressDTO
import it.polito.wa2.g13.crm.dtos.CreateAddressDTO
import jakarta.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["civic", "street", "city", "postal_code"])])
class Address(
    var civic: String,
    var street: String,
    var city: String,
    var postalCode: String,
    var country: String,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "contact_address",
        joinColumns = [JoinColumn(name = "address_id", referencedColumnName = "id", foreignKey = ForeignKey())],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id", foreignKey = ForeignKey())]
    )
    var contacts: MutableSet<Contact>,
) : BaseEntity() {
    companion object {
        @JvmStatic
        fun from(address: CreateAddressDTO): Address = Address(
            civic = address.civic,
            street = address.street,
            city = address.city,
            postalCode = address.postalCode,
            country = address.country,
            contacts = mutableSetOf(),
        )

        @JvmStatic
        fun from(address: AddressDTO): Address = Address(
            civic = address.civic,
            street = address.street,
            city = address.city,
            postalCode = address.postalCode,
            country = address.country,
            contacts = mutableSetOf(),
        )
    }
}