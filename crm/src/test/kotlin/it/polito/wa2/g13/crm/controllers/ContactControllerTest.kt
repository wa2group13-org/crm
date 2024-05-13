package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.dtos.ContactDTO
import it.polito.wa2.g13.crm.dtos.CreateContactDTO
import it.polito.wa2.g13.crm.dtos.CreateEmailDTO
import it.polito.wa2.g13.crm.dtos.EmailDTO
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.services.ContactServiceImplTest
import it.polito.wa2.g13.crm.utils.ResultPage
import it.polito.wa2.g13.crm.utils.randomContacts
import it.polito.wa2.g13.crm.utils.randomEmails
import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
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
class ContactControllerTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(ContactServiceImplTest::class.java)

        private val contacts = randomContacts(10, 5)

    }

    @BeforeEach
    fun setupDatabase(@Autowired contactService: ContactService) {
        contacts.forEach { contactService.createContact(it) }
        logger.info("Created contacts for integration test")
    }

    @Autowired
    private lateinit var restClient: TestRestTemplate

    @Test
    fun `get should retrieve contacts`() {
        val limit = 10
        val page = 0

        val req = RequestEntity
            .get("/API/contacts?limit=$limit&page=$page")
            .build()

        val res = restClient.exchange<ResultPage<ContactDTO>>(req)

        assertEquals(true, res.statusCode.is2xxSuccessful)
        Assertions.assertThat(res.body?.map { CreateContactDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(contacts)
    }

    @Test
    fun `post should create a new contact!`() {
        val req = RequestEntity
            .post("/API/contacts")
            .body(randomContacts(1, 4)[0], CreateContactDTO::class.java)

        val res = restClient.exchange<ContactDTO>(req)

        assertEquals(true, res.statusCode.is2xxSuccessful)
    }

    @Test
    fun `update should create a new entity if it doesn't exist`() {
        val newContact = randomContacts(1, 4)[0]

        val updateRes = restClient.exchange<Unit>(
            RequestEntity
                .put("/API/contacts/-1")
                .body(newContact, CreateContactDTO::class.java)
        )

        assertNotEquals(null, updateRes.headers.location)

        val newIndex = updateRes.headers.location!!.toString().split("/").last().toLong()

        val updatedContact = restClient.exchange<ContactDTO>(
            RequestEntity
                .get("/API/contacts/$newIndex")
                .build()
        )
            .body

        Assertions.assertThat(updatedContact?.let { CreateContactDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(newContact)
    }

    @Test
    fun `delete created contact`() {
        val contactToDelete = restClient.exchange<ResultPage<ContactDTO>>(
            RequestEntity
                .get("/API/contacts?page=0&limit=1")
                .build()
        ).body!!.first()

        val res = restClient.exchange<Unit>(
            RequestEntity
                .delete("/API/contacts/${contactToDelete.id}")
                .build()
        )

        assertTrue(res.statusCode.is2xxSuccessful)
    }

    private fun prepareEmails(): Pair<ContactDTO, List<EmailDTO>> {
        val contact = restClient.exchange<ResultPage<ContactDTO>>(
            RequestEntity
                .get("/API/contacts?page=0&limit=1")
                .build()
        ).body!!.first()

        val emails = restClient.exchange<List<EmailDTO>>(
            RequestEntity
                .get("/API/contacts/${contact.id}/emails")
                .build()
        ).body!!

        return Pair(contact, emails)
    }

    @Test
    fun `get all emails of a contact`() {
        val (contact, emails) = prepareEmails()

        val validEmails = contacts.first { it.name == contact.name }.emails

        Assertions.assertThat(emails.map { CreateEmailDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(validEmails)
    }

    @Test
    fun `add an email to a contact`() {
        val (contact, emails) = prepareEmails()

        val emailToAdd = randomEmails(1).first()

        val res = restClient.exchange<Unit>(
            RequestEntity
                .post("/API/contacts/${contact.id}/emails")
                .body(emailToAdd, CreateEmailDTO::class.java)
        )

        val finalContactEmails = restClient.exchange<List<EmailDTO>>(
            RequestEntity
                .get("/API/contacts/${contact.id}/emails")
                .build()
        ).body!!

        assertTrue(res.statusCode.is2xxSuccessful)
        Assertions.assertThat(finalContactEmails.map { CreateEmailDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(emails.map { CreateEmailDTO.from(it) } + emailToAdd)
    }

    @Test
    fun `delete an email from a contact`() {
        val (contact, emails) = prepareEmails()

        val emailToRemove = emails.first().id

        val res = restClient.exchange<Unit>(
            RequestEntity
                .delete("/API/contacts/${contact.id}/emails/$emailToRemove")
                .build()
        )

        val finalContactEmails = restClient.exchange<List<EmailDTO>>(
            RequestEntity
                .get("/API/contacts/${contact.id}/emails")
                .build()
        ).body!!

        assertTrue(res.statusCode.is2xxSuccessful)
        Assertions.assertThat(finalContactEmails.map { CreateEmailDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(emails.filter { it.id != emailToRemove }.map { CreateEmailDTO.from(it) })
    }

    @Test
    fun `update an email from a contact should create a new one and delete the previous one`() {
        val (contact, emails) = prepareEmails()

        val emailToUpdate = emails.first().id
        val newEmail = randomEmails(1).first()

        val res = restClient.exchange<Unit>(
            RequestEntity
                .put("/API/contacts/${contact.id}/emails/$emailToUpdate")
                .body(newEmail, CreateEmailDTO::class.java)
        )

        val finalContactEmails = restClient.exchange<List<EmailDTO>>(
            RequestEntity
                .get("/API/contacts/${contact.id}/emails")
                .build()
        ).body!!

        assertTrue(res.statusCode.is2xxSuccessful)
        Assertions.assertThat(finalContactEmails.map { CreateEmailDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(
                emails
                    .filter { it.id != emailToUpdate }
                    .map { CreateEmailDTO.from(it) }
                    .toMutableList()
                    .apply { add(newEmail) }
            )
    }
}