package it.polito.wa2.g13.crm.data.professional

import it.polito.wa2.g13.crm.data.BaseEntity
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
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

    var notes: String?,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(referencedColumnName = "id", name = "contact_id", foreignKey = ForeignKey())
    var contact: Contact,
): BaseEntity() {
    companion object {
        @JvmStatic
        fun from(professional: CreateProfessionalDTO): Professional = Professional(
            employmentState = professional.employmentState,
            dailyRate = professional.dailyRate,
            skills = professional.skills.toMutableSet(),
            notes = professional.notes,
            contact = Contact.from(professional.contact)
        )
    }
}