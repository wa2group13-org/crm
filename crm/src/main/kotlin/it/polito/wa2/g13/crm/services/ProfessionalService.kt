package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import kotlin.jvm.Throws

interface ProfessionalService {
    fun getProfessionals(page: Int, limit: Int, professionalFilters: ProfessionalFilters): List<ProfessionalDTO>

    fun createProfessional(professionalDto: CreateProfessionalDTO): Long

    @Throws(ProfessionalException.NotFound::class)
    fun getProfessional(id: Long): ProfessionalDTO

    fun updateProfessional(id: Long, professionalDto: CreateProfessionalDTO): Long?
}