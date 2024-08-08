package it.polito.wa2.g13.crm.data.contact

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.dtos.CreateTelephoneDTO
import jakarta.persistence.*

@Entity
class Telephone(
    @Column(unique = true)
    var number: String,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "contact_telephone",
        joinColumns = [JoinColumn(name = "telephone_id", referencedColumnName = "id", foreignKey = ForeignKey())],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id", foreignKey = ForeignKey())]
    )
    var contacts: MutableSet<Contact>,
) : BaseEntity() {
    companion object {
        @JvmStatic
        fun from(telephone: CreateTelephoneDTO): Telephone = Telephone(
            number = telephone.number,
            contacts = mutableSetOf(),
        )
    }
}