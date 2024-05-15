package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.dtos.JobOfferDTO
import it.polito.wa2.g13.crm.dtos.JobOfferFilters
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.services.CustomerService
import it.polito.wa2.g13.crm.services.JobOfferService
import it.polito.wa2.g13.crm.services.JobOfferServiceImplTest
import it.polito.wa2.g13.crm.utils.ResultPage
import it.polito.wa2.g13.crm.utils.randomCategorizedContacts
import it.polito.wa2.g13.crm.utils.randomCustomers
import it.polito.wa2.g13.crm.utils.randomJobOffers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
    private lateinit var jobOfferService: JobOfferService

    @Autowired
    private lateinit var restClient: TestRestTemplate

    fun initJobOffers(n: Int) {
        val contacts = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        contactsIds.addAll(contacts.map { contactService.createContact(it) }.toList())

        randomCustomers(contactService.getContacts(0, 10, null, null, null).toList())
        customersIds.addAll(
            contactsIds.map {
                customerService.createCustomer(it).id
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

        val reqURI = UriComponentsBuilder.fromUriString(baseURI)
            .queryParam("page", page)
            .queryParam("limit", limit)
            .queryParam("filters.byStatus", statuses)
            .queryParam("filters.byCustomerId", filters.byCustomerId)
            .queryParam("filters.byProfessionalId", filters.byProfessionalId)
            .build().toUri()

        val req = RequestEntity.get(reqURI).build()
        val res = restClient.exchange<ResultPage<JobOfferDTO>>(req)

        assertTrue(res.statusCode.is2xxSuccessful)
        assertEquals(1, res.body?.content?.size)
        assertThat(listOf(jobOffers[0]))
            .usingRecursiveComparison()
            .ignoringCollectionOrderInFields("skills", "notes")
            .ignoringFields("notes.logTime")
            .isEqualTo(res.body?.content)
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

}