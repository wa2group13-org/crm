package it.polito.wa2.g13.crm.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProfessionalDTO(
    @field:DecimalMin("0.0")
    val dailyRate: Double,
    val employmentState: EmploymentState,
    @field:Size(max = 100, min = 1)
    @field:Valid
    val skills: Set<@Valid CreateSkillDTO>,
    @field:NotBlank
    @field:Size(max = 5000)
    val notes: String?,
    @field:Min(0)
    val contactId: Long,
    /**
     * If the contact information is provided with [CreateContactDTO]
     * a new contact will be created and [contactId] will be ignored.
     */
    @field:Valid
    val contactInfo: CreateContactDTO?,
) {
    companion object {
        @JvmStatic
        fun from(professional: ProfessionalDTO): CreateProfessionalDTO = CreateProfessionalDTO(
            dailyRate = professional.dailyRate,
            employmentState = professional.employmentState,
            skills = professional.skills.map { CreateSkillDTO.from(it) }.toSet(),
            notes = professional.notes,
            contactId = professional.contact.id,
            contactInfo = null,
        )
    }
}

/**
 * With [JsonCreator] and [JsonValue] it's possible to parse
 * this class a string, so the final result will be just
 * a [Set] of [String]
 */
data class CreateSkillDTO @JsonCreator constructor(
    @field:NotBlank
    @field:Size(max = 255)
    @get:JsonValue
    val skill: String,
) {
    companion object {
        @JvmStatic
        fun from(skill: String): CreateSkillDTO = CreateSkillDTO(skill = skill)
    }
}