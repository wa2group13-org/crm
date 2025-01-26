package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.data.joboffer.PROFIT_MARGIN
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.services.*
import it.polito.wa2.g13.crm.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = ["/scripts/clean_db.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JobOfferControllerTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(JobOfferServiceImplTest::class.java)
        private val contactsIds = mutableListOf<Long>()
        private val customersIds = mutableListOf<Long>()
        private val jobOffers = mutableListOf<JobOfferDTO>()
    }

    @Autowired
    private lateinit var contactService: ContactService

    @Autowired
    private lateinit var customerService: CustomerService

    @Autowired
    private lateinit var professionalService: ProfessionalService

    @Autowired
    private lateinit var jobOfferService: JobOfferService

    @Autowired
    private lateinit var restClient: TestRestTemplate

    fun initJobOffers(n: Int) {
        val contacts = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        contactsIds.addAll(contacts.map { contactService.createContact(it).id }.toList())

        customersIds.addAll(
            contactsIds.map {
                customerService.createCustomer(CreateCustomerDTO.from(it)).id
            }.toMutableList()
        )

        val createJobOffers = randomJobOffers(customersIds, n, JobOfferStatus.Created)
        jobOffers.addAll(createJobOffers.map { jobOfferService.createJobOffer(it) }.toMutableList())

        logger.info("Initialized DB")
    }

    @BeforeEach
    fun setupDatabase() {
        contactsIds.clear()
        customersIds.clear()
        jobOffers.clear()
    }

    @Test
    fun `get jobOffers by params should return some jobsOffers`() {
        initJobOffers(3)

        val limit = 10
        val page = 0

        val customerId = jobOffers[0].customerId
        val professionalId = jobOffers[0].professionalId

        val filters = JobOfferFilters(customerId, professionalId, setOf(JobOfferStatus.Created))

        val baseURI = "/API/joboffers"
        val statuses = filters.byStatus?.toList()?.joinToString { it.toString() }

        val reqURI = UriComponentsBuilder.fromUriString(baseURI).queryParam("page", page).queryParam("limit", limit)
            .queryParam("filters.byStatus", statuses).queryParam("filters.byCustomerId", filters.byCustomerId)
            .queryParam("filters.byProfessionalId", filters.byProfessionalId).build().toUri()

        val req = RequestEntity.get(reqURI).build()
        val res = restClient.exchange<ResultPage<JobOfferDTO>>(req)

        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(1, res.body?.content?.size)
        assertThat(listOf(jobOffers[0])).usingRecursiveComparison().ignoringCollectionOrderInFields("skills", "notes")
            .ignoringFields("notes.logTime").isEqualTo(res.body?.content)
    }

    @Test
    fun `get jobOffers by params should fail for invalid request`() {
        val uri = "/API/joboffers?limit=12"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<String>(req)
        assertTrue(res.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
    }

    @Test
    fun `get jobOffers by params should fail for invalid request in filter`() {
        val uri = "/API/joboffers?limit=10"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<String>(req)
        assertTrue(res.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
    }

    @Test
    fun `getJobOffer by id should return a job offer`() {
        initJobOffers(1)
        val uri = "/API/joboffers/${jobOffers[0].id}"
        val req = RequestEntity.get(uri).build()
        val res = restClient.exchange<JobOfferDTO>(req)

        assertTrue(res.statusCode.is2xxSuccessful)
        assertThat(jobOffers[0]).usingRecursiveComparison().ignoringFields("notes.logTime").ignoringCollectionOrder()
            .isEqualTo(res.body)
    }

    @Test
    fun `getJobOffer should fail for bad id`() {
        val uri = "/API/joboffers/potato"
        val req = RequestEntity.get(uri).build()
        val res = restClient.exchange<String>(req)
        logger.info(res.body)
        logger.info(res.statusCode.toString())

        assertTrue(res.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
    }

    @Test
    fun `createJobOffer should successfully create a job offer`() {
        val contactId = contactService.createContact(randomCategorizedContact(category = ContactCategory.Unknown)).id
        val customer = customerService.createCustomer(CreateCustomerDTO.from(contactId))

        val jobOffer = randomJobOffer(customer.id, 3, JobOfferStatus.Created)
        val uri = "/API/joboffers"
        val req = RequestEntity.post(uri).body(jobOffer, CreateJobOfferDTO::class.java)
        val res = restClient.exchange<JobOfferDTO>(req)

        assertTrue(res.statusCode.is2xxSuccessful)
    }

    @Test
    fun `getJobOfferValue should fail because no professional is assigned`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val uri = "/API/joboffers/${id}/value"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<String>(req)

        assertTrue(res.statusCode.is4xxClientError)
    }

    @Test
    fun `getJobOfferValue should retrieve the value`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val contactId = contactService.createContact(randomCategorizedContact(category = ContactCategory.Unknown)).id
        val professional = professionalService.getProfessional(
            professionalService.createProfessional(
                randomProfessional(contactId, 1).copy(employmentState = EmploymentState.Available, dailyRate = 12.5)
            ).id
        )
        jobOfferService.updateJobOfferStatus(id, UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null))
        jobOfferService.updateJobOfferStatus(id, UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, null))
        jobOfferService.updateJobOfferStatus(
            id, UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professional.id, null)
        )

        val uri = "/API/joboffers/${id}/value"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<Double>(req)

        assertTrue(res.statusCode.is2xxSuccessful)
        assertNotNull(res.body)
        res.body?.let {
            assertEquals(it, professional.dailyRate * jobOffers[0].duration * PROFIT_MARGIN, 0.001)
        }
    }

    @Test
    fun `changeJobOfferStatus should change the status`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val target = JobOfferStatus.SelectionPhase
        val note = "changed!!"

        val uri = "/API/joboffers/${id}"
        val body = UpdateJobOfferStatusDTO(target, null, note)
        val req = RequestEntity.post(uri)
            .body(body, UpdateJobOfferStatusDTO::class.java)

        val res = restClient.exchange<JobOfferDTO>(req)
        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(res.body?.status, target)
    }

    @Test
    fun `changeJobOfferDetails should change the details`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val description = "new job description"
        val skill = setOf(CreateSkillDTO("Docker"))

        val uri = "/API/joboffers/${id}/details"
        val body = UpdateJobOfferDetailsDTO(description, skill, 123)
        val req = RequestEntity.put(uri)
            .body(body, UpdateJobOfferDetailsDTO::class.java)

        val res = restClient.exchange<JobOfferDTO>(req)
        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(res.body?.description, description)
        assertThat(res.body?.skills)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(skill.map { it.skill }.toSet())
    }

    @Test
    fun `deleteJobOffer should successfully remove a jobOffer`() {
        initJobOffers(1)

        val id = jobOffers[0].id
        val uri = "/API/joboffers/${id}"
        val req = RequestEntity.delete(uri).build()

        val res = restClient.exchange<Unit>(req)
        assertTrue(res.statusCode.is2xxSuccessful)
    }

    @Test
    fun `getNote by id should return a note`() {
        initJobOffers(1)
        val note = "second note"
        val newJobOffer = jobOfferService.updateJobOfferStatus(
            jobOffers[0].id,
            UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, note)
        )

        val history = newJobOffer.notes.filter { it.currentStatus == JobOfferStatus.SelectionPhase }[0]

        val uri = "/API/joboffers/${newJobOffer.id}/notes/${history.id}"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<JobOfferHistoryDTO>(req)
        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(res.body?.note, note)
    }

    @Test
    fun `getNotes by id should return all the notes of the giver joboffer`() {
        initJobOffers(1)
        val contactId = contactService.createContact(randomCategorizedContact(category = ContactCategory.Unknown)).id
        val professional = professionalService.getProfessional(
            professionalService.createProfessional(
                randomProfessional(contactId, 1).copy(employmentState = EmploymentState.Available, dailyRate = 12.5)
            ).id
        )
        jobOfferService.updateJobOfferStatus(
            jobOffers[0].id,
            UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, "first note")
        )
        jobOfferService.updateJobOfferStatus(
            jobOffers[0].id,
            UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, "second note")
        )
        val newJobOffer = jobOfferService.updateJobOfferStatus(
            jobOffers[0].id,
            UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, professional.id, "third note")
        )

        val uri = "/API/joboffers/${newJobOffer.id}/notes"
        val req = RequestEntity.get(uri).build()

        val res = restClient.exchange<List<JobOfferHistoryDTO>>(req)
        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(4, res.body?.size)

        assertThat(res.body?.map {
            it.note
        })
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(newJobOffer.notes.map { it.note })
    }

    @Test
    fun `addNote by jobOffer id should add a new note (not related to a status change)`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val uri = "/API/joboffers/${id}/notes"
        val body = CreateJobOfferHistoryNoteDTO("my very clever note")

        val req = RequestEntity.post(uri).body(body, CreateJobOfferHistoryNoteDTO::class.java)
        val res = restClient.exchange<JobOfferHistoryDTO>(req)
        assertTrue(res.statusCode.is2xxSuccessful)

        val note = jobOfferService.getJobOfferById(id).notes.filter { it.id == res.body?.id }[0]

        assertThat(note).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields("logTime")
            .isEqualTo(res.body)

    }

    @Test
    fun `updateNoteById should change an existing note`() {
        initJobOffers(1)
        val id = jobOffers[0].id
        val noteId = jobOffers[0].notes[0].id

        val uri = "/API/joboffers/${id}/notes/${noteId}"
        val body = CreateJobOfferHistoryNoteDTO("JobOffer created for fun :) ")

        val req = RequestEntity.put(uri).body(body, CreateJobOfferHistoryNoteDTO::class.java)
        val res = restClient.exchange<JobOfferHistoryDTO>(req)

        logger.info(res.body.toString())
        assertTrue(res.statusCode.is2xxSuccessful)

        val note = jobOfferService.getJobOfferById(id).notes.filter { it.id == res.body?.id }[0]

        assertThat(note).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields("logTime")
            .isEqualTo(res.body)
    }

    @Test
    fun `assign job offer to a professional already assigned`() {
        initJobOffers(2)
        val id1 = jobOffers[0].id
        val id2 = jobOffers[1].id
        val p1 = professionalService.createProfessional(
            randomProfessional(
                0,
                1
            ).copy(
                employmentState = EmploymentState.Available,
                contactInfo = randomContacts(1, 1)[0].copy(category = ContactCategory.Unknown)
            )
        )


        fun changeStatus(s: JobOfferStatus, id: Long, pId: Long? = null) {
            val uri = "/API/joboffers/${id}"

            val body = UpdateJobOfferStatusDTO(s, pId, "note")
            val req = RequestEntity.post(uri)
                .body(body, UpdateJobOfferStatusDTO::class.java)

            val res = restClient.exchange<JobOfferDTO>(req)
            assertTrue(res.statusCode.is2xxSuccessful)
            assertEquals(res.body?.status, s)
            assertEquals(res.body?.professionalId, pId)
        }

        changeStatus(JobOfferStatus.SelectionPhase, id1)
        changeStatus(JobOfferStatus.CandidateProposal, id1)
        changeStatus(JobOfferStatus.Consolidated, id1, p1.id)

        changeStatus(JobOfferStatus.SelectionPhase, id2)
        changeStatus(JobOfferStatus.CandidateProposal, id2)

        val uri = "/API/joboffers/${id2}"
        val body = UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, id2, "note")
        val req = RequestEntity.post(uri)
            .body(body, UpdateJobOfferStatusDTO::class.java)

        val res = restClient.exchange<Any>(req)
        assertTrue(res.statusCode.is4xxClientError)
    }
}









