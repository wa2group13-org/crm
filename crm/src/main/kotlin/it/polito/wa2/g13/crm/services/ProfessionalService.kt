package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters

interface ProfessionalService {
    fun getProfessionals(page: Int, limit: Int, professionalFilters: ProfessionalFilters): List<ProfessionalDTO>
}