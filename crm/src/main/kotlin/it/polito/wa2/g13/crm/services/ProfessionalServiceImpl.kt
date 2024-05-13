package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.CreateSkillDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.repositories.ContactRepository
import it.polito.wa2.g13.crm.repositories.ProfessionalRepository
import it.polito.wa2.g13.crm.utils.nullable
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProfessionalServiceImpl(
    val professionalRepository: ProfessionalRepository,
    val contactRepository: ContactRepository,
) : ProfessionalService {
    companion object {
        private val logger = LoggerFactory.getLogger(ProfessionalServiceImpl::class.java)
    }

    private fun createProfessionalEntity(professionalDto: CreateProfessionalDTO): Professional {
        val contact =
            contactRepository.findById(professionalDto.contactId).nullable() ?: throw ContactException.NotFound.from(
                professionalDto.contactId
            )

        return Professional.from(professionalDto, contact)
    }

    override fun getProfessionals(
        page: Int,
        limit: Int,
        professionalFilters: ProfessionalFilters
    ): Page<ProfessionalDTO> {
        return professionalRepository
            .findAll(
                Professional.Spec.withFilters(professionalFilters),
                PageRequest.of(page, limit)
            )
            .map { ProfessionalDTO.from(it) }
    }

    override fun createProfessional(professionalDto: CreateProfessionalDTO): Long {
        val professional = createProfessionalEntity(professionalDto)

        val saveProfessional = professionalRepository.save(professional)

        logger.info("${::createProfessional.name}: Created Professional@${saveProfessional.id}")

        return saveProfessional.id
    }

    override fun getProfessional(id: Long): ProfessionalDTO {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        return ProfessionalDTO.from(professional)
    }

    override fun updateProfessional(id: Long, professionalDto: CreateProfessionalDTO): Long? {
        val professional = professionalRepository.findById(id).nullable() ?: return createProfessional(professionalDto)

        val newProfessional = createProfessionalEntity(professionalDto)

        professional.update(newProfessional)

        professionalRepository.save(professional)

        logger.info("${::updateProfessional.name}: Updated Professional@${id}")

        return null
    }

    override fun deleteProfessional(id: Long) {
        if (!professionalRepository.existsById(id))
            throw ProfessionalException.NotFound.from(id)

        professionalRepository.deleteById(id)
    }

    override fun updateProfessionalNotes(id: Long, notesDto: String) {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        professional.notes = notesDto

        professionalRepository.save(professional)
    }

    override fun updateProfessionalEmploymentState(id: Long, employmentState: EmploymentState) {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        professional.employmentState = employmentState

        professionalRepository.save(professional)
    }

    override fun updateProfessionalDailyRate(id: Long, dailyRate: Double) {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        professional.dailyRate = dailyRate

        professionalRepository.save(professional)
    }

    override fun updateProfessionalSkills(id: Long, skillsDto: Set<CreateSkillDTO>) {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        professional.skills = skillsDto.map { it.skill }.toMutableSet()

        professionalRepository.save(professional)
    }

    override fun updateProfessionalContact(id: Long, contactId: Long) {
        val professional =
            professionalRepository.findById(id).nullable() ?: throw ProfessionalException.NotFound.from(id)

        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)

        professional.contact = contact

        professionalRepository.save(professional)
    }
}