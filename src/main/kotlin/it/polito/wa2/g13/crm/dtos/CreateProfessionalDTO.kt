package it.polito.wa2.g13.crm.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import jakarta.validation.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AllowedEnumValidator::class])
private annotation class AllowedState(
    val message: String = "Invalid value. This is not permitted.",
    val allowed: Array<EmploymentState> = [],
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * This need to validate the specific enum values we want to allow
 */
private class AllowedEnumValidator : ConstraintValidator<AllowedState, EmploymentState> {
    private lateinit var allowed: List<EmploymentState>

    override fun initialize(annotation: AllowedState) {
        super.initialize(annotation)
        allowed = annotation.allowed.toList() // Get all enum constants
    }

    override fun isValid(value: EmploymentState?, context: ConstraintValidatorContext?): Boolean {
        // Allow null values by returning true if value is null
        if (value == null) return true

        // Check if the value matches any of the enum values
        return allowed.any { it == value }
    }

}

data class CreateProfessionalDTO(
    @field:DecimalMin("0.0")
    val dailyRate: Double,
    @field:AllowedState(
        allowed = [EmploymentState.Available, EmploymentState.NotAvailable],
        message = "Invalid value. Only 'Available' and 'NotAvailable' are allowed."
    )
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