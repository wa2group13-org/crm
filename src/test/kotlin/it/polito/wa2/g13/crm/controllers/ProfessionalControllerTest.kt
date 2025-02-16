package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.CreateContactDTO
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.services.ProfessionalService
import it.polito.wa2.g13.crm.utils.ResultPage
import it.polito.wa2.g13.crm.utils.assertRecursive
import it.polito.wa2.g13.crm.utils.randomContacts
import it.polito.wa2.g13.crm.utils.randomProfessional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.RequestEntity
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = ["/scripts/clean_db.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessionalControllerTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(ProfessionalController::class.java)
    }

    @Autowired
    private lateinit var contactService: ContactService

    @Autowired
    private lateinit var professionalService: ProfessionalService

    private val contacts = randomContacts(10, 5).map { it.copy(category = ContactCategory.Unknown) }
    private val professionals = mutableListOf<CreateProfessionalDTO>()
    private val professionalIds = mutableListOf<Long>()

    @BeforeEach
    fun createDb() {
        professionalIds.clear()
        professionals.clear()

        contacts.forEach { contact ->
            val contactDTO = contactService.createContact(contact)

            val professional = randomProfessional(contactDTO.id, 5)
            professionals.add(professional)

            val newProfessional = professionalService.createProfessional(professional)
            professionalIds.add(newProfessional.id)
        }

        logger.info("Initialized DB")
    }

    fun newProfessional(): Pair<CreateProfessionalDTO, CreateContactDTO> {
        val contact = randomContacts(1, 5)[0].copy(category = ContactCategory.Unknown)
        val contactDTO = contactService.createContact(contact)
        return Pair(randomProfessional(contactDTO.id, 5), contact)
    }


    @Autowired
    private lateinit var restClient: TestRestTemplate

    @Test
    fun `get all professionals should match inserted ones`() {
        val limit = 10
        val page = 0

        val req = RequestEntity
            .get("/API/professionals?limit=$limit&page=$page")
            .build()

        val res = restClient.exchange<ResultPage<ProfessionalDTO>>(req)



        assertEquals(true, res.statusCode.is2xxSuccessful)
        assertRecursive(professionals, res.body?.map { CreateProfessionalDTO.from(it) })
    }

    @Test
    fun `get by id should return the inserted professional`() {
        val professional = restClient
            .exchange<ResultPage<ProfessionalDTO>>(
                RequestEntity.get("/API/professionals?limit=10&page=0").build()
            )
            .body!!
            .first()

        val res = restClient.exchange<ProfessionalDTO>(
            RequestEntity.get("/API/professionals/${professional.id}").build()
        )

        assertTrue(res.statusCode.is2xxSuccessful)
        assertRecursive(CreateProfessionalDTO.from(professional), res.body?.let { CreateProfessionalDTO.from(it) })
    }

    @Test
    fun `a created professional should be retrievable by id`() {
        val (professional, _) = newProfessional()

        val professionalId = restClient
            .exchange<ProfessionalDTO>(
                RequestEntity.post("/API/professionals").body(professional, CreateProfessionalDTO::class.java)
            )
            .headers
            .location
            ?.toString()
            ?.split("/")
            ?.last()
            ?.toLongOrNull()

        assertNotEquals(null, professionalId)

        val createdProfessional = restClient
            .exchange<ProfessionalDTO>(RequestEntity.get("/API/professionals/$professionalId").build())
            .body!!

        assertRecursive(professional, CreateProfessionalDTO.from(createdProfessional))
    }

    @Test
    fun `a deleted professional should not exist`() {
        val id = professionalIds.first()

        val res = restClient.exchange<Unit>(RequestEntity.delete("/API/professionals/$id").build())

        assertTrue(res.statusCode.is2xxSuccessful)

        val res404 = restClient.exchange<Any>(RequestEntity.get("/API/professionals/$id").build())

        assertTrue(res404.statusCode.is4xxClientError)
    }

    @Test
    fun `update professional should succeed`() {
        val id = professionalIds.first()
        val (newProfessional, _) = newProfessional()

        val res = restClient.exchange<Unit>(
            RequestEntity.put("/API/professionals/$id").body(newProfessional, CreateProfessionalDTO::class.java)
        )

        assertTrue(res.statusCode.is2xxSuccessful)

        val updateProfessional = restClient
            .exchange<ProfessionalDTO>(RequestEntity.get("/API/professionals/$id").build())
            .body!!

        assertRecursive(newProfessional, CreateProfessionalDTO.from(updateProfessional))
    }

    @Test
    fun `updating a professional that doesn't exist should create a new one`() {
        val (newProfessional, _) = newProfessional()
        val newId = restClient
            .exchange<ProfessionalDTO>(
                RequestEntity.put("/API/professionals/-1").body(newProfessional, CreateProfessionalDTO::class.java)
            )
            .headers
            .location
            ?.toString()
            ?.split("/")
            ?.lastOrNull()
            ?.toLongOrNull()

        assertNotEquals(null, newId)

        val createdProfessional = restClient
            .exchange<ProfessionalDTO>(RequestEntity.get("/API/professionals/$newId").build())
            .body

        assertRecursive(newProfessional, createdProfessional?.let { CreateProfessionalDTO.from(it) })
    }

    @Test
    fun `update a professional fields should succeed`() {
        val id = professionalIds.first()
        val (newProfessional, _) = newProfessional()

        restClient.exchange<Any>(
            RequestEntity.put("/API/professionals/$id/notes").body(newProfessional.notes ?: "")
        )

        restClient.exchange<Any>(
            RequestEntity.put("/API/professionals/$id/skills").body(newProfessional.skills)
        )

        restClient.exchange<Any>(
            RequestEntity.put("/API/professionals/$id/employmentState").body(newProfessional.employmentState)
        )

        restClient.exchange<Any>(
            RequestEntity.put("/API/professionals/$id/dailyRate").body(newProfessional.dailyRate)
        )

        restClient.exchange<Any>(
            RequestEntity.put("/API/professionals/$id/contact").body(newProfessional.contactId)
        )

        logger.info("Get the final Professional@$id, the contact will be excluded")

        val updatedProfessional = restClient
            .exchange<ProfessionalDTO>(
                RequestEntity.get("/API/professionals/$id").build()
            )
            .body

        assertRecursive(newProfessional, updatedProfessional?.let { CreateProfessionalDTO.from(it) })
    }

    // TODO: decide what to do with this kind of operation
//    @Test
//    fun `cannot insert a professional with Employed state`() {
//        val professional = newProfessional().first.copy(employmentState = EmploymentState.Employed)
//
//        val error = restClient
//            .exchange<Any>(
//                RequestEntity.post("/API/professionals").body(professional, CreateProfessionalDTO::class.java)
//            )
//
//        assertTrue(error.statusCode.is4xxClientError)
//    }
}