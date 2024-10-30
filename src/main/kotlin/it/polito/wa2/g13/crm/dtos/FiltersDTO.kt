package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class LocationFilter(
    @field:Size(min = 1, max = 255)
    val byCity: String?,
    @field:Size(min = 1, max = 255)
    val byPostalCode: String?,
    @field:Size(min = 1, max = 255)
    val byStreet: String?,
    @field:Size(min = 1, max = 255)
    val byCivic: String?,
    @field:Size(min = 1, max = 255)
    val byCountry: String?,
)

data class ProfessionalFilters(
    @field:Valid
    @field:Size(max = 100)
    val bySkills: Set<String>?,
    val byEmploymentState: EmploymentState?,
    @field:Valid
    val byLocation: LocationFilter?,
    @field:Size(min = 1, max = 255)
    val byFullName: String?,
)

data class CustomerFilters(
    @field:Valid
    val byLocation: LocationFilter?,
    @field:Size(min = 1, max = 255)
    val byFullName: String?,
)