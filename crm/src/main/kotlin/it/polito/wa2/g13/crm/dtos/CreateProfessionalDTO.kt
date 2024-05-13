package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProfessionalDTO(
    @field:DecimalMin("0.0")
    val dailyRate: Double,
    val employmentState: EmploymentState,
    @field:Size(max = 100, min = 1)
    @field:Valid
    val skills: Set<CreateSkillDTO>,
    @field:NotBlank
    @field:Size(max = 255)
    val notes: String?,
    val contactId: Long,
) {
    companion object {
        @JvmStatic
        fun from(professional: ProfessionalDTO): CreateProfessionalDTO = CreateProfessionalDTO(
            dailyRate = professional.dailyRate,
            employmentState = professional.employmentState,
            skills = professional.skills.map { CreateSkillDTO.from(it) }.toSet(),
            notes = professional.notes,
            contactId = professional.contact.id,
        )
    }
}

data class CreateSkillDTO(
    @field:NotBlank
    @field:Size(max = 255)
    val skill: String,
) {
    companion object {
        @JvmStatic
        fun from(skill: String): CreateSkillDTO = CreateSkillDTO(skill = skill)
    }
}