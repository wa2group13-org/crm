package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.JobOfferException
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class JobOfferServiceImplTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(JobOfferServiceImplTest::class.java)
    }

    @Autowired
    lateinit var contactService: ContactService

    @Autowired
    lateinit var customerService: CustomerService

    @Autowired
    lateinit var jobOfferService: JobOfferService

    @Autowired
    lateinit var professionalService: ProfessionalService

    fun initJobOffers(n: Int): Pair<MutableList<Long>, MutableList<CreateJobOfferDTO>> {
        val contacts = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsIds = contacts.map { contactService.createContact(it).id }.toList()
//        randomCustomers(contactService.getContacts(0, 10, null, null, null))
        val customersIds = contactsIds.map { customerService.createCustomer(CreateCustomerDTO.from(it)).id }.toList()
        val jobOffers = randomJobOffers(customersIds, n, JobOfferStatus.Created)
        val jobOffersIds = jobOffers.map { jobOfferService.createJobOffer(it).id }.toMutableList()

        return Pair(jobOffersIds, jobOffers)
    }

    fun initConsolidatedJobOffers(n: Int): Pair<MutableList<Long>, MutableList<CreateJobOfferDTO>> {
        val contactsC = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsCIds = contactsC.map { contactService.createContact(it).id }.toList()
        val contactsP = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsPIds = contactsP.map { contactService.createContact(it).id }.toList()

        val customerIds = contactsCIds.map { customerService.createCustomer(CreateCustomerDTO.Companion.from(it)).id }
        val newProfessionals = contactsPIds.map {
            professionalService.createProfessional(
                // A professional should be available in order to be assigned to a jobOffer
                randomProfessional(
                    it,
                    n
                ).copy(employmentState = EmploymentState.Available)
            )
        }

        val jobOffers = randomJobOffers(customerIds, n, JobOfferStatus.CandidateProposal)
        val jobOffersIds = jobOffers.map { jobOfferService.createJobOffer(it).id }.toMutableList()

        jobOffersIds.zip(newProfessionals).forEach {
            jobOfferService.updateJobOfferStatus(
                it.first,
                UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, it.second.id, "Candidate Consolidated!")
            )
        }

        val jobOfferDTOs =
            jobOfferService.getJobOffersByParams(null, 0, n).toList().map { CreateJobOfferDTO.from(it) }.toMutableList()

        return Pair(jobOffersIds, jobOfferDTOs)
    }

    private fun initProfessional(randomRelations: Int): ProfessionalDTO {
        val contact = randomContacts(1, randomRelations)[0].copy(category = ContactCategory.Unknown)
        val contactId = contactService.createContact(contact).id
        // A contact should be available in order to be assigned to a jobOffer
        val professional =
            randomProfessional(contactId, randomRelations).copy(employmentState = EmploymentState.Available)
        return professionalService.createProfessional(professional)
    }

    private fun initCandidateProposalJobOffers(randomRelations: Int): Pair<List<Long>, List<CreateJobOfferDTO>> {
        val pair = initJobOffers(randomRelations)

        pair.first.forEach { id ->
            jobOfferService.updateJobOfferStatus(
                id, UpdateJobOfferStatusDTO(
                    status = JobOfferStatus.SelectionPhase,
                    null,
                    null,
                )
            )
        }

        pair.first.forEach { id ->
            jobOfferService.updateJobOfferStatus(
                id, UpdateJobOfferStatusDTO(
                    status = JobOfferStatus.CandidateProposal,
                    null,
                    null,
                )
            )
        }

        logger.info("created $randomRelations jobOffers at CandidateProposal state")

        return pair
    }

    @Test
    fun `should retrieve all job offers`() {
        val (_, _) = initJobOffers(7)

        val res = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(7, res.content.size)
    }

    @Test
    fun `get with status filters should return 3 elements`() {
        val (ids, _) = initJobOffers(8)

        val updated = ids.take(3).map {
            jobOfferService.updateJobOfferStatus(
                it,
                UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
            )
        }

        val filters = mutableSetOf<JobOfferStatus>()
        filters.add(JobOfferStatus.SelectionPhase)
        val result = jobOfferService.getJobOffersByParams(JobOfferFilters(byStatus = filters), 0, 10)

        assertEquals(3, result.content.size)
        updated.forEachIndexed { id, jobOfferDTO ->
            assertThat(result.content[id]).usingRecursiveComparison().ignoringFields("id").isEqualTo(jobOfferDTO)
        }
    }

    @Test
    fun `should retrieve a job offer by id`() {
        val (jobOffersIds, jobOffers) = initJobOffers(4)

        jobOffersIds.forEachIndexed { index, id ->
            val jobOffer = jobOfferService.getJobOfferById(id)

            assertEquals(jobOffers[index], CreateJobOfferDTO.from(jobOffer))
        }
    }

    @Test
    fun `should create a new job offer`() {
        val (ids, jobOffers) = initJobOffers(5)
        val contact = randomCategorizedContact(category = ContactCategory.Unknown)
        val contactId = contactService.createContact(contact).id
        randomCustomer(contactService.getContactById(contactId))
        val customer = customerService.createCustomer(CreateCustomerDTO.Companion.from(contactId))
        val newJobOffer = randomJobOffer(customer.id, null, null)

        val newJobOfferDTO = jobOfferService.createJobOffer(newJobOffer)
        jobOffers.add(newJobOffer)
        ids.add(newJobOfferDTO.id)

        val result = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(6, result.content.size)
        assertThat(result.content).contains(newJobOfferDTO)

    }

    @Test
    fun `should retrieve the value of a jobOffer given its id`() {
        val (ids, _) = initConsolidatedJobOffers(5)
        val randomId = ids.random()

        val jobOffer = jobOfferService.getJobOfferById(randomId)
        val result = jobOfferService.getJobOfferValue(randomId)

        assertEquals(jobOffer.value, result)
    }

    @Test
    fun `should update job offer details given its id`() {
        val (ids, _) = initJobOffers(5)
        val randomId = ids.random()

        val updateJobOfferDetailsDTO = UpdateJobOfferDetailsDTO("Modified", randomSkills(4).toSet(), 200)
        jobOfferService.updateJobOfferDetails(randomId, updateJobOfferDetailsDTO)

        val result = jobOfferService.getJobOfferById(randomId)
        assertEquals(updateJobOfferDetailsDTO.skills.map { it.skill }.toSet(), result.skills)
        assertEquals(updateJobOfferDetailsDTO.duration, result.duration)
        assertEquals(updateJobOfferDetailsDTO.description, result.description)

    }

    @Test
    fun `should update job offer status given its id`() {
        val (ids, _) = initJobOffers(2)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        val filter1 = setOf(JobOfferStatus.SelectionPhase)
        val result1 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter1), 0, 10)

        val updateJobOfferStatusDTO2 = UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO2)
        val filter2 = setOf(JobOfferStatus.CandidateProposal)
        val result2 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter2), 0, 10)


        assertEquals(updateJobOfferStatusDTO1.status, result1.content[0].status)
        assertEquals(updateJobOfferStatusDTO2.status, result2.content[0].status)
    }

    @Test
    fun `should go back to SelectionPhase from Consolidated status`() {
        val (ids, _) = initConsolidatedJobOffers(2)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        val filter1 = setOf(JobOfferStatus.SelectionPhase)
        val result1 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter1), 0, 10)

        assertEquals(updateJobOfferStatusDTO1.status, result1.content[0].status)
    }

    @Test
    fun `should delete a job offer`() {
        val (ids, jobOffers) = initConsolidatedJobOffers(2)
        val randomId = ids.random()

        jobOfferService.deleteJobOffer(randomId)

        val result = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertRecursive(
            jobOffers.zip(ids).filter { it.second != randomId }.map { it.first },
            result.content.map { CreateJobOfferDTO.from(it) })

    }

    // FROM NOW ON TESTS FOR JOB OFFER HISTORY
    @Test
    fun `should retrieve a list of job offer history`() {
        val (ids, _) = initJobOffers(2)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        val updateJobOfferStatusDTO2 = UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, null)

        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO2)

        val result = jobOfferService.getNotesByJobOfferId(randomId)

        assertEquals(3, result.size)
    }

    @Test
    fun `should retrieve a note given its id`() {
        val (ids, _) = initJobOffers(3)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        val updateJobOfferStatusDTO2 = UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, null)

        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO2)

        val notes = jobOfferService.getNotesByJobOfferId(randomId)

        notes.forEach {
            val result = jobOfferService.getNoteById(randomId, it.id)
            assertEquals(it, result)
        }
    }

    @Test
    fun `should add a note given job offer id`() {
        val (ids, _) = initJobOffers(3)
        val randomId = ids.random()

        val createJobOfferHistoryNoteDTO = CreateJobOfferHistoryNoteDTO(
            "Selection Phase"
        )

        val addedNote = jobOfferService.addNoteByJobOfferId(randomId, createJobOfferHistoryNoteDTO)
        val result = jobOfferService.getNotesByJobOfferId(randomId)
        val note = jobOfferService.getNoteById(randomId, addedNote.id)

        assertEquals(2, result.size)
        assertEquals(addedNote, note)
    }

    @Test
    fun `should update an existent note given its id`() {
        val (ids, _) = initJobOffers(3)
        val randomId = ids.random()

        val createJobOfferHistoryNoteDTO = CreateJobOfferHistoryNoteDTO(
            "Created and modified"
        )
        val note = jobOfferService.getNotesByJobOfferId(randomId).first()
        val updated = jobOfferService.updateNoteById(randomId, note.id, createJobOfferHistoryNoteDTO)
        val updatedNote = jobOfferService.getNoteById(randomId, note.id)

        assertThat(updatedNote)
            .usingRecursiveComparison()
            .ignoringFields("note")
            .isEqualTo(note)
        assertEquals(updated, updatedNote)

    }

    // --- JobOffer with Professional Test ---

    @Test
    fun `updating the status of job offer to consolidated with a professional that doesn't exist should throw`() {
        val (jobOfferIds, _) = initCandidateProposalJobOffers(1)

        assertThrows<ProfessionalException.NotFound> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds.first(),
                UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, 0, null)
            )
        }
    }

    @Test
    fun `updating the status of a job offer to consolidated with a professional that is already assigned should throw`() {
        val (jobOfferIds, _) = initCandidateProposalJobOffers(2)
        val professionalId = initProfessional(5).id

        // Assign the professional to the first job offer
        jobOfferService.updateJobOfferStatus(
            jobOfferIds[0],
            UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professionalId, null)
        )

        assertThrows<JobOfferException.IllegalProfessionalState> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[1],
                UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professionalId, null)
            )
        }
    }

    @Test
    fun `updating the status of a job offer from consolidated to done without the same professional should succeed`() {
        val (jobOfferIds, _) = initCandidateProposalJobOffers(1)
        val professionalId = initProfessional(5).id
        val availableProfessionalId = initProfessional(5).id

        // Assign the professional to the first job offer
        jobOfferService.updateJobOfferStatus(
            jobOfferIds[0],
            UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professionalId, null)
        )

        assertThrows<JobOfferException.ForbiddenTargetStatus> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.Done, professionalId, null)
            )
        }

        assertThrows<JobOfferException.ForbiddenTargetStatus> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.Done, availableProfessionalId, null)
            )
        }

        val newJobOffer = jobOfferService.updateJobOfferStatus(
            jobOfferIds[0],
            UpdateJobOfferStatusDTO(JobOfferStatus.Done, null, null)
        )

        assertEquals(newJobOffer.professionalId, null)
        assertEquals(newJobOffer.status, JobOfferStatus.Done)
    }

    @Test
    fun `updating the status of a job offer from consolidated to abort with a non-null professional should throw`() {
        val (jobOfferIds, _) = initCandidateProposalJobOffers(1)
        val professionalId = initProfessional(5).id

        // Assign the professional to the first job offer
        jobOfferService.updateJobOfferStatus(
            jobOfferIds[0],
            UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professionalId, null)
        )

        assertThrows<JobOfferException.ForbiddenTargetStatus> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.Aborted, professionalId, null)
            )
        }

        assertDoesNotThrow {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.Aborted, null, null)
            )
        }

        assertEquals(JobOfferStatus.Aborted, jobOfferService.getJobOfferById(jobOfferIds[0]).status)
    }

    @Test
    fun `updating the status of a job offer from consolidated to selectionPhase with a non-null professional should throw`() {
        val (jobOfferIds, _) = initCandidateProposalJobOffers(1)
        val professionalId = initProfessional(4).id

        // Assign the professional to the first job offer
        jobOfferService.updateJobOfferStatus(
            jobOfferIds[0],
            UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professionalId, null)
        )

        assertThrows<JobOfferException.ForbiddenTargetStatus> {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, professionalId, null)
            )
        }

        assertDoesNotThrow {
            jobOfferService.updateJobOfferStatus(
                jobOfferIds[0],
                UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
            )
        }

        assertEquals(EmploymentState.Available, professionalService.getProfessional(professionalId).employmentState)
        assertEquals(JobOfferStatus.SelectionPhase, jobOfferService.getJobOfferById(jobOfferIds[0]).status)
    }
}