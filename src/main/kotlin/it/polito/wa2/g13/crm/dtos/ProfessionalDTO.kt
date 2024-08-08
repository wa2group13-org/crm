package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.data.professional.Professional

data class ProfessionalDTO(
    val id: Long,
    val dailyRate: Double,
    val employmentState: EmploymentState,
    val skills: Set<String>,
    val notes: String?,
    val contact: ContactDTO,
) {
    companion object {
        @JvmStatic
        fun from(professional: Professional): ProfessionalDTO = ProfessionalDTO(
            id = professional.id,
            dailyRate = professional.dailyRate,
            employmentState = professional.employmentState,
            skills = professional.skills.toSet(),
            notes = professional.notes,
            contact = ContactDTO.from(professional.contact),
        )
    }
}
