package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStateMachine
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.CustomerException
import it.polito.wa2.g13.crm.exceptions.JobOfferException
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.repositories.CustomerRepository
import it.polito.wa2.g13.crm.repositories.JobOfferRepository
import it.polito.wa2.g13.crm.repositories.ProfessionalRepository
import it.polito.wa2.g13.crm.utils.nullable
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val professionalRepository: ProfessionalRepository,
    private val customerRepository: CustomerRepository
) : JobOfferService {
    companion object {
        private val logger = LoggerFactory.getLogger(JobOfferServiceImpl::class.java)
    }

    override fun getJobOfferById(id: Long): JobOfferDTO {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)
        return JobOfferDTO.from(jobOffer)
    }

    override fun getJobOfferValue(id: Long): Double {
        val offer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)
        if (offer.value == null) {
            throw JobOfferException.MissingProfessional.from(id)
        }
        return offer.value
    }

    override fun getJobOffersByParams(filters: JobOfferFilters?, page: Int, limit: Int): Page<JobOfferDTO> {
        return jobOfferRepository.findAll(
            JobOffer.Spec.withFilters(filters),
            PageRequest.of(page, limit)
        ).map { JobOfferDTO.from(it) }
    }

    override fun createJobOffer(createJobOfferDTO: CreateJobOfferDTO): JobOfferDTO {
        val customer = customerRepository.findById(createJobOfferDTO.customerId).nullable()
            ?: throw CustomerException.NotFound.from(createJobOfferDTO.customerId)
        val jobOffer = JobOffer(
            customer = customer,
            duration = createJobOfferDTO.duration,
            status = createJobOfferDTO.status,
            professional = null,
            description = createJobOfferDTO.description,
            skills = createJobOfferDTO.skills.map { it.skill }.toMutableSet(),
            notes = mutableSetOf()
        )
        jobOfferRepository.save(jobOffer)
        logger.info("JobOffer ${jobOffer.id} created")
        return JobOfferDTO.from(jobOffer)
    }

    override fun updateJobOfferDetails(id: Long, updateJobOfferDetailsDTO: UpdateJobOfferDetailsDTO): JobOfferDTO {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)

        jobOffer.update(updateJobOfferDetailsDTO) //update the details (description, skills, duration)

        jobOfferRepository.save(jobOffer)
        logger.info("JobOffer ${jobOffer.id} details updated")
        return JobOfferDTO.from(jobOffer)
    }

    override fun updateJobOfferStatus(id: Long, updateJobOfferStatusDTO: UpdateJobOfferStatusDTO): JobOfferDTO {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)

        val professional = updateJobOfferStatusDTO.professionalId?.let {
            professionalRepository.findById(it).nullable() ?: throw ProfessionalException.NotFound.from(it)
        }

        //check if the transition is feasible according to the state machine
        val isTargetStateFeasible =
            JobOfferStateMachine(jobOffer.status).isStatusFeasible(updateJobOfferStatusDTO.status)
        if (!isTargetStateFeasible)
            throw JobOfferException.ForbiddenTargetStatus.from(
                jobOffer.status,
                updateJobOfferStatusDTO.status
            )

        //change the professional state.
        if (professional != null) {
            //when the status is Consolidated, a Professional must be present
            if (updateJobOfferStatusDTO.status == JobOfferStatus.Consolidated) {
                jobOffer.professional = professional
                professional.employmentState = EmploymentState.Employed
                logger.info("professional ${professional.id} assigned to JobOffer ${jobOffer.id}")
            }
            //modelling the "rollback" of the state machine from (Consolidated, Done, CandidateProposal??)
            else if (updateJobOfferStatusDTO.status == JobOfferStatus.SelectionPhase &&
                jobOffer.status != JobOfferStatus.Created
            ) {
                jobOffer.professional = null
                professional.employmentState = EmploymentState.Available
                logger.info("JobOffer ${jobOffer.id} professional ${professional.id} removed")
            }
            professionalRepository.save(professional)
        }
        jobOffer.status = updateJobOfferStatusDTO.status

        val note = JobOfferHistory(
            jobOffer,
            assignedProfessional = professional,
            logTime = OffsetDateTime.now(),
            currentStatus = jobOffer.status,
            note = updateJobOfferStatusDTO.note
        )
        jobOffer.notes.add(note)
        jobOfferRepository.save(jobOffer)

        logger.info("JobOffer ${jobOffer.id} status changed to ${jobOffer.status}")
        return JobOfferDTO.from(jobOffer)
    }

    override fun deleteJobOffer(id: Long) {
        jobOfferRepository.deleteById(id)
        logger.info("JobOffer $id deleted")
    }

    override fun getNoteById(jobOfferId: Long, noteId: Long): JobOfferHistoryDTO {
        val offer =
            jobOfferRepository.findById(jobOfferId).nullable() ?: throw JobOfferException.NotFound.from(jobOfferId)
        val note = offer.notes.find { it.id == noteId }
        if (note == null) {
            throw JobOfferException.NoteNotFound.from(noteId)
        }
        return JobOfferHistoryDTO.from(note)

    }

    override fun getNotesByJobOfferId(id: Long): List<JobOfferHistoryDTO> {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)
        return jobOffer.notes.map {
            JobOfferHistoryDTO.from(it)
        }.sortedByDescending { it.logTime }
    }

    override fun addNoteByJobOfferId(id: Long, note: CreateJobOfferHistoryDTO): JobOfferHistoryDTO {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)
        var assignedProfessional: Professional? = null
        if (note.assignedProfessional != null) {
            assignedProfessional = professionalRepository.findById(note.assignedProfessional).nullable()
                ?: throw ProfessionalException.NotFound.from(note.assignedProfessional)
        }
        val addedNote = JobOfferHistory(
            jobOffer,
            assignedProfessional,
            OffsetDateTime.now(),
            jobOffer.status,
            note.note
        )
        jobOffer.notes.add(addedNote)
        jobOfferRepository.save(jobOffer)
        logger.info("Note ${addedNote.id} added to JobOffer ${jobOffer.id}")
        return JobOfferHistoryDTO.from(addedNote)
    }

    override fun updateNoteById(jobOfferId: Long, noteId: Long, note: String?): JobOfferHistoryDTO {
        val jobOffer =
            jobOfferRepository.findById(jobOfferId).nullable() ?: throw JobOfferException.NotFound.from(jobOfferId)

        val noteUpdated = jobOffer.notes.find { it.id == noteId }

        if (noteUpdated == null) {
            throw JobOfferException.NoteNotFound.from(noteId)
        }

        noteUpdated.note = note
        jobOfferRepository.save(jobOffer)

        logger.info("Note ${noteUpdated.id} updated")
        return JobOfferHistoryDTO.from(noteUpdated)
    }

}