package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStateMachine
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.CustomerException
import it.polito.wa2.g13.crm.exceptions.JobOfferException
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.repositories.CustomerRepository
import it.polito.wa2.g13.crm.repositories.JobOfferHistoryRepository
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
    private val customerRepository: CustomerRepository,
    private val jobOfferHistoryRepository: JobOfferHistoryRepository
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

        return offer.value ?: throw JobOfferException.MissingProfessional.from(id)
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
        val note = JobOfferHistory(
            jobOffer,
            assignedProfessional = null,
            logTime = OffsetDateTime.now(),
            currentStatus = jobOffer.status,
            note = "System Created"
        )
        jobOffer.notes.add(note)

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
            JobOfferStateMachine(
                jobOffer.status,
                jobOffer.professional?.id
            ).isStatusFeasible(updateJobOfferStatusDTO.status, updateJobOfferStatusDTO.professionalId)

        if (!isTargetStateFeasible) {
            logger.error("${::updateJobOfferDetails.name}: The transition from ${jobOffer.status} to ${updateJobOfferStatusDTO.status} was not possible!")
            throw JobOfferException.ForbiddenTargetStatus.from(
                jobOffer.status,
                updateJobOfferStatusDTO.status
            )
        }

        // Check that the professional is in Available state
        professional?.employmentState?.let {
            if (it != EmploymentState.Available && professional != jobOffer.professional) {
                logger.error("Tried to assign ${Professional::class.qualifiedName}@${professional.id} to ${JobOffer::class.qualifiedName}@${jobOffer.id}, but it was already assigned!")
                throw JobOfferException.IllegalProfessionalState.from(jobOffer.id, professional.id)
            }
        }

        // When we reach this point we are sure that the professional can be
        // assigned or removed from the jobOffer
        if (professional != null) {
            //when the status is Consolidated, a Professional must be present
            jobOffer.professional = professional
            professional.jobOffer = jobOffer
            professional.employmentState = EmploymentState.Employed
            logger.info("${Professional::class.qualifiedName}@${professional.id} assigned to ${JobOffer::class.qualifiedName}@${jobOffer.id}")
        } else {
            //modelling the "rollback" of the state machine from (Consolidated, Done, CandidateProposal??)
            jobOffer.professional?.apply {
                this.employmentState = EmploymentState.Available
                this.jobOffer = null
                logger.info("Removed ${Professional::class.qualifiedName}@${this.id} from ${JobOffer::class.qualifiedName}@${jobOffer.id}")
            }
            jobOffer.professional = null
        }

        jobOffer.status = updateJobOfferStatusDTO.status

        val note = JobOfferHistory(
            jobOffer,
            assignedProfessional = jobOffer.professional,
            logTime = OffsetDateTime.now(),
            currentStatus = jobOffer.status,
            note = updateJobOfferStatusDTO.note
        )
        jobOffer.notes.add(note)
        jobOffer.professional?.jobOfferHistory?.add(note)

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

    override fun addNoteByJobOfferId(id: Long, note: CreateJobOfferHistoryNoteDTO): JobOfferHistoryDTO {
        val jobOffer = jobOfferRepository.findById(id).nullable() ?: throw JobOfferException.NotFound.from(id)

        val addedNote = JobOfferHistory(
            jobOffer,
            jobOffer.professional,
            OffsetDateTime.now(),
            jobOffer.status,
            note.note
        )
        jobOffer.notes.add(addedNote)
        val newNote = jobOfferHistoryRepository.save(addedNote)
        val newJobOffer = jobOfferRepository.save(jobOffer)
        logger.info("Note ${newNote.id} added to JobOffer ${newJobOffer.id}")
        return JobOfferHistoryDTO.from(newNote)
    }

    override fun updateNoteById(
        jobOfferId: Long,
        noteId: Long,
        note: CreateJobOfferHistoryNoteDTO
    ): JobOfferHistoryDTO {
        val jobOffer =
            jobOfferRepository.findById(jobOfferId).nullable() ?: throw JobOfferException.NotFound.from(jobOfferId)

        val noteUpdated = jobOffer.notes.find { it.id == noteId }

        if (noteUpdated == null) {
            throw JobOfferException.NoteNotFound.from(noteId)
        }

        noteUpdated.note = note.note
        jobOfferRepository.save(jobOffer)

        logger.info("Note ${noteUpdated.id} updated")
        return JobOfferHistoryDTO.from(noteUpdated)
    }

}