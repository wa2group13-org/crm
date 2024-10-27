package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.exceptions.CustomerException
import it.polito.wa2.g13.crm.utils.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CustomerServiceImplTest : IntegrationTest() {
    companion object {
        //        private val logger = LoggerFactory.getLogger(CustomerServiceImplTest::class.java)
        private val createContacts = randomContacts(10, 5).map { it.copy(category = ContactCategory.Unknown) }
        private val _contacts = mutableListOf<ContactDTO>()
        private val contacts: List<ContactDTO> = _contacts
        private val _customers = mutableListOf<CustomerDTO>()
        private val customers: List<CustomerDTO> = _customers

        @JvmStatic
        @BeforeAll
        fun init(@Autowired contactService: ContactService, @Autowired customerService: CustomerService) {
            createContacts.forEach { createContactDTO ->
                val contactDTO = contactService.createContact(createContactDTO)
                val customerDTO = customerService.createCustomer(CreateCustomerDTO.from(contactDTO.id))
                _contacts.add(contactDTO)
                _customers.add(customerDTO)
            }
        }
    }

    @Autowired
    lateinit var contactService: ContactService

    @Autowired
    lateinit var customerService: CustomerService

    @Test
    fun `get all customers, expected 200`() {
        val testCustomers = customerService.getCustomers(0, 50)
        assertRecursive(customers, testCustomers)
    }

    @Test
    fun `get a customer by id`() {
        val testCustomerDTO = customerService.getCustomerById(customers[0].id)
        assertRecursive(customers[0], testCustomerDTO)
    }

    @Test
    fun `get customer by invalid or absent id`() {
        assertThrows<CustomerException.NotFound> { customerService.getCustomerById(-1) }
    }

    @Test
    fun `create new valid customer`() {
        val contactDTO = contactService.createContact(randomContact().copy(category = ContactCategory.Unknown))
        val customerDTO = customerService.createCustomer(CreateCustomerDTO.Companion.from(contactDTO.id))
        val testCustomerDTO = customerService.getCustomerById(customerDTO.id)
        assertRecursive(customerDTO, testCustomerDTO)
    }

    @Test
    fun `create customer with contact`() {
        val contact = randomContacts(1, 10).first().copy(category = ContactCategory.Unknown)
        val customer = randomCustomerWithContact(contact)

        val newCustomer = customerService.createCustomer(customer)

        assertRecursive(
            contact.copy(category = ContactCategory.Customer),
            CreateContactDTO.from(newCustomer.contact)
        )

        Assertions.assertThat(customer)
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .withIgnoredFields("contactId", "contactInfo")
                    .build()
            )
            .isEqualTo(CreateCustomerDTO.Companion.from(newCustomer))
    }

    @Test
    fun `failed to create a new customer because contact id is absent`() {
        assertThrows<ContactException.NotFound> { customerService.createCustomer(CreateCustomerDTO.from(-1)) }
    }

    @Test
    fun `failing to create a customer, contact already linked to someone else`() {
        assertThrows<CustomerException.ContactAlreadyTaken> {
            customerService.createCustomer(
                CreateCustomerDTO.from(
                    contacts[0].id
                )
            )
        }
    }

    @Test
    fun `delete customer`() {
        customerService.deleteCustomerById(customers[0].id)
        assertThrows<CustomerException.NotFound> { customerService.getCustomerById(customers[0].id) }
    }

    @Test
    fun `delete non existent customer`() {
        assertThrows<CustomerException.NotFound> { customerService.getCustomerById(44) }
    }

    @Test
    fun `update customer note`() {
        val note = "My Test Note"
        customerService.updateCustomerNote(customers[0].id, note)
        val customerDTO = customerService.getCustomerById(customers[0].id)
        assertEquals(note, customerDTO.note)
    }

    @Test
    fun `update absent customer note`() {
        assertThrows<CustomerException.NotFound> { customerService.updateCustomerNote(-1, "note") }
    }

    @Test
    fun `update customer contact`() {
        val contactDTO = contactService.createContact(randomContact().copy(category = ContactCategory.Unknown))
        customerService.updateCustomerContact(customers[0].id, contactDTO.id)
        val customerDTO = customerService.getCustomerById(customers[0].id)
        assertRecursive(contactDTO, customerDTO.contact)
    }

    @Test
    fun `update customer contact with an absent contact`() {
        assertThrows<ContactException.NotFound> { customerService.updateCustomerContact(customers[0].id, -1) }
    }

    @Test
    fun `update customer contact with an already linked contact`() {
        assertThrows<CustomerException.ContactAlreadyTaken> {
            customerService.updateCustomerContact(
                customers[0].id,
                customers[1].contact.id
            )
        }
    }
}