package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.professional.EmploymentState

data class LocationFilter(
    val byCity: String?,
    val byPostalCode: String?,
    val byStreet: String?,
    val byCivic: String?,
)

data class ProfessionalFilters(
    val bySkills: Set<String>?,
    val byEmploymentState: EmploymentState?,
    val byLocation: LocationFilter?,
)
