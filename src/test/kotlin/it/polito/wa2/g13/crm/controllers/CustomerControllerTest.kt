package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.services.CustomerService
import it.polito.wa2.g13.crm.services.CustomerServiceImpl
import it.polito.wa2.g13.crm.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.RequestEntity
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = ["/scripts/clean_db.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CustomerControllerTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(CustomerServiceImpl::class.java)
    }

    @Autowired
    private lateinit var contactService: ContactService

    @Autowired
    private lateinit var customerService: CustomerService

    @Autowired
    private lateinit var restClient: TestRestTemplate


    fun addRandomContactToDB() : ContactDTO{
        val createContactDTO = randomContact().copy(category = ContactCategory.Unknown)
        val req1 = RequestEntity.post("/API/contacts").body(createContactDTO, CreateContactDTO::class.java)
        val res1 = restClient.exchange<ContactDTO>(req1)

        assertNotNull(res1.body)
        return res1.body!!
    }

    private val createContacts = randomContacts(10, 5).map { it.copy(category = ContactCategory.Unknown) }
    private val customers = mutableListOf<CustomerDTO>()
    private val contacts = mutableListOf<ContactDTO>()



    @BeforeEach
    fun init() {
        customers.clear()
        contacts.clear()
        createContacts.forEach { contact ->
            val contactDTO = contactService.createContact(contact)
            val customerDTO = customerService.createCustomer(CreateCustomerDTO.from(contactDTO.id))
            contacts.add(contactDTO)
            customers.add(customerDTO)
        }
        logger.info("Initialized DB")

    }

    @Test
    fun `get all customers`() {
        val limit = 10
        val page = 0
        val req = RequestEntity.get("/API/customers?limit=$limit&page=$page").build()
        val res = restClient.exchange<ResultPage<CustomerDTO>>(req)

        assertEquals(HttpStatus.OK, res.statusCode)
        assertRecursive(customers, res.body)
    }

    @Test
    fun `get customer by invalid id`() {
        val id = -1
        val req = RequestEntity.get("/API/customers/$id").build()
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `get customer by valid id`() {
        val id = customers[0].id
        val req = RequestEntity.get("/API/customers/$id").build()
        val res = restClient.exchange<CustomerDTO>(req)

        assertEquals(HttpStatus.OK, res.statusCode)
        assertRecursive(customers[0], res.body)
    }

    @Test
    fun `create new valid customer`() {
        val createContactDTO = randomContact().copy(category = ContactCategory.Unknown)
        val req1 = RequestEntity.post("/API/contacts").body(createContactDTO, CreateContactDTO::class.java)
        val res1 = restClient.exchange<ContactDTO>(req1)

        assertNotNull(res1.body)

        val contactDTO = res1.body!!
        val req2 = RequestEntity.post("/API/customers").body(ContactIdDTO(contactDTO.id), ContactIdDTO::class.java)
        val res2 = restClient.exchange<CustomerDTO>(req2)

        assertNotNull(res2.body)
        val customerDTO = res2.body!!

        assertEquals(contactDTO.copy(category = ContactCategory.Customer), customerDTO.contact)
    }

    @Test
    fun `failing to create customer because contact id not valid 404`() {
        val req = RequestEntity.post("/API/customers").body(ContactIdDTO(-1), ContactIdDTO::class.java)
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `failing to create a customer, contact already linked to someone else`() {
        val createContactDTO = randomContact().copy(category = ContactCategory.Customer)
        val req1 = RequestEntity.post("/API/contacts").body(createContactDTO, CreateContactDTO::class.java)
        val res1 = restClient.exchange<ContactDTO>(req1)

        assertNotNull(res1.body)

        val contactDTO = res1.body!!
        val req2 = RequestEntity.post("/API/customers").body(ContactIdDTO(contactDTO.id), ContactIdDTO::class.java)
        val res2 = restClient.exchange<ProblemDetail>(req2)

        assertEquals(HttpStatus.FORBIDDEN, res2.statusCode)
    }

    @Test
    fun `delete customer, expected 204`() {
        val customerId = customers[0].id
        val req1 = RequestEntity.delete("/API/customers/$customerId").build()
        val res1 = restClient.exchange<Unit>(req1)

        assertEquals(HttpStatus.NO_CONTENT, res1.statusCode)

        val req2 = RequestEntity.get("/API/customers/$customerId").build()
        val res2 = restClient.exchange<ProblemDetail>(req2)

        assertEquals(HttpStatus.NOT_FOUND, res2.statusCode)
    }

    @Test
    fun `delete non existent customer, expected 404`() {
        val req = RequestEntity.get("/API/customers/-1").build()
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `update customer note, expected 200`() {
        val customerId = customers[0].id
        val note = "My Test Note"
        val req = RequestEntity.put("/API/customers/$customerId/note").body(CustomerNoteDTO("My Test Note"))
        val res = restClient.exchange<Unit>(req)

        assertEquals(HttpStatus.OK, res.statusCode)

        val req2 = RequestEntity.get("/API/customers/$customerId").build()
        val res2 = restClient.exchange<CustomerDTO>(req2)

        val customerDTO = res2.body!!

        assertEquals(note, customerDTO.note)
    }

    @Test
    fun `update absent customer note, expected 404 `() {
        val customerId = -1
//        val note = "My Test Note"
        val req = RequestEntity.put("/API/customers/$customerId/note").body(CustomerNoteDTO("My Test Note"))
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `update customer contact, expected 200`() {
        val contactDTO = addRandomContactToDB()
        val customerId = customers[0].id
        val req = RequestEntity.put("/API/customers/$customerId/contact").body(ContactIdDTO(contactDTO.id))
        val res = restClient.exchange<Unit>(req)

        assertEquals(HttpStatus.OK, res.statusCode)

        val req2 = RequestEntity.get("/API/customers/$customerId").build()
        val res2 = restClient.exchange<CustomerDTO>(req2)

        val customerDTO = res2.body!!

        assertEquals(contactDTO, customerDTO.contact)
    }

    @Test
    fun `update customer contact with an absent contact, expected 404` () {
        val customerId = customers[0].id
        val req = RequestEntity.put("/API/customers/$customerId/contact").body(ContactIdDTO(-1))
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `update customer contact with an already linked contact, expected 403` () {
        val customerId = customers[0].id
        val req = RequestEntity.put("/API/customers/$customerId/contact").body(ContactIdDTO(customers[1].contact.id))
        val res = restClient.exchange<ProblemDetail>(req)

        assertEquals(HttpStatus.FORBIDDEN, res.statusCode)
    }

}