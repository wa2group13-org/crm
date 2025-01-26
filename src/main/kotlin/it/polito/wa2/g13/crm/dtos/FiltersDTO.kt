package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class LocationFilter(
    @field:Size(max = 255)
    val byCity: String?,
    @field:Size(max = 255)
    val byPostalCode: String?,
    @field:Size(max = 255)
    val byStreet: String?,
    @field:Size(max = 255)
    val byCivic: String?,
    @field:Size(max = 255)
    val byCountry: String?,
)

data class ProfessionalFilters(
    @field:Valid
    @field:Size(max = 100)
    val bySkills: List<String>?,
    val byEmploymentState: EmploymentState?,
    @field:Valid
    val byLocation: LocationFilter?,
    @field:Size(max = 255)
    val byFullName: String?,
    @field:Valid
    val withState: List<EmploymentState>?,
    @field:Valid
    val withoutState: List<EmploymentState>?,
) {
    companion object {
        fun empty() = ProfessionalFilters(
            bySkills = null,
            byEmploymentState = null,
            byLocation = null,
            byFullName = null,
            withState = null,
            withoutState = null,
        )
    }
}

data class CustomerFilters(
    @field:Valid
    val byLocation: LocationFilter?,
    @field:Size(max = 255)
    val byFullName: String?,
)
