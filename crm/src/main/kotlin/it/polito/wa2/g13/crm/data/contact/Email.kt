package it.polito.wa2.g13.crm.data.contact

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.dtos.CreateEmailDTO
import it.polito.wa2.g13.crm.dtos.EmailDTO
import jakarta.persistence.*

@Entity
class Email(
    @Column(unique = true)
    var email: String,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "contact_email",
        joinColumns = [JoinColumn(name = "email_id", referencedColumnName = "id", foreignKey = ForeignKey())],
        inverseJoinColumns = [JoinColumn(name = "contact_id", referencedColumnName = "id", foreignKey = ForeignKey())]
    )
    var contacts: MutableSet<Contact>,
) : BaseEntity() {
    companion object {
        @JvmStatic
        fun from(email: CreateEmailDTO): Email = Email(
            email = email.email,
            contacts = mutableSetOf(),
        )

        fun from(email: EmailDTO): Email = Email(
            email = email.email,
            contacts = mutableSetOf(),
        ).apply {
            id = email.id
        }
    }
}