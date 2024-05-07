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
    val skills: Set<String>,
    @field:NotBlank
    @field:Size(max = 255)
    val notes: String?,
    @field:Valid
    val contact: CreateContactDTO,
)