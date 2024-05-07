package it.polito.wa2.g13.crm.data

import it.polito.wa2.g13.crm.data.contact.Contact
import jakarta.persistence.*

enum class EmploymentState {
    Employed,
    Available,
    NotAvailable,
}

@Entity
class Professional(
    @Enumerated
    var employmentState: EmploymentState,

    var dailyRate: Double,

    @ElementCollection
    var skills: MutableSet<String>,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(referencedColumnName = "id", name = "contact_id", foreignKey = ForeignKey())
    var contact: Contact,
): BaseEntity()