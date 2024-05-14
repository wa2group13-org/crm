package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.services.ContactServiceImplTest
import it.polito.wa2.g13.crm.services.CustomerService
import it.polito.wa2.g13.crm.services.CustomerServiceImpl
import it.polito.wa2.g13.crm.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.http.ResponseEntity
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


    private val contacts = randomContacts(10, 5).map { it.copy(category = ContactCategory.Unknown) }
    private val customers = mutableListOf<CustomerDTO>()

    @BeforeEach
    fun init(){
        customers.clear()
        contacts.forEach{ contact ->
            val contactId = contactService.createContact(contact)
            val customer = customerService.createCustomer(contactId)
            customers.add(customer)
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

        assertEquals( HttpStatus.NOT_FOUND, res.statusCode)
    }

    @Test
    fun `get customer by valid id`(){
        val id = customers[0].id
        val req = RequestEntity.get("/API/customers/$id").build()
        val res = restClient.exchange<CustomerDTO>(req)

        assertEquals( HttpStatus.OK, res.statusCode)
        assertRecursive(customers[0], res.body)
    }

    @Test
    fun `create new customer`(){
        val createContact = randomContact()
        val req1 = RequestEntity.post("/API/contacts").body(createContact, CreateContactDTO::class.java)
        val res1 = restClient.exchange<RequestEntity<*>>(req1)

        //val req2 = RequestEntity.get(res1.headers.location).build()
        //val contact = restClient.exchange<ContactDTO>(req2)
    }

}