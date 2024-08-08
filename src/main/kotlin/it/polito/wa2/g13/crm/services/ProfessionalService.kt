package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.CreateSkillDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import org.springframework.data.domain.Page

interface ProfessionalService {
    fun getProfessionals(page: Int, limit: Int, professionalFilters: ProfessionalFilters): Page<ProfessionalDTO>

    fun createProfessional(professionalDto: CreateProfessionalDTO): ProfessionalDTO

    @Throws(ProfessionalException.NotFound::class)
    fun getProfessional(id: Long): ProfessionalDTO

    fun updateProfessional(id: Long, professionalDto: CreateProfessionalDTO): ProfessionalDTO?

    @Throws(ProfessionalException.NotFound::class)
    fun deleteProfessional(id: Long)

    @Throws(ProfessionalException.NotFound::class)
    fun updateProfessionalNotes(id: Long, notesDto: String)

    @Throws(ProfessionalException.NotFound::class)
    fun updateProfessionalEmploymentState(id: Long, employmentState: EmploymentState)

    @Throws(ProfessionalException.NotFound::class)
    fun updateProfessionalDailyRate(id: Long, dailyRate: Double)

    @Throws(ProfessionalException.NotFound::class)
    fun updateProfessionalSkills(id: Long, skillsDto: Set<CreateSkillDTO>)

    fun updateProfessionalContact(id: Long, contactId: Long)
}