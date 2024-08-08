package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.dtos.CreateAddressDTO
import it.polito.wa2.g13.crm.dtos.CreateContactDTO
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.utils.randomAddresses
import it.polito.wa2.g13.crm.utils.randomContacts
import it.polito.wa2.g13.crm.utils.randomEmails
import it.polito.wa2.g13.crm.utils.randomTelephones
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.min

@SpringBootTest
@Transactional
class ContactServiceImplTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(ContactServiceImplTest::class.java)

        private val telephones = randomTelephones(6)
        private val emails = randomEmails(6)
        private val addresses = randomAddresses(6)

        private val contacts = randomContacts(6, null).map {
            it.copy(
                telephones = telephones,
                emails = emails,
                addresses = addresses
            )
        }
    }

    @Autowired
    lateinit var contactService: ContactService

    @Test
    fun `should retrieve all paged elements inserted`() {
        val limit = 4
        var i = 0

        contacts.forEach {
            contactService.createContact(it)

            val result = contactService.getContacts(0, limit, null, null, null)

            i += 1
            assertEquals(min(i, limit), result.content.size)
        }
    }

    @Test
    fun `get with telephone filter should retrieve 2 element`() {
        val telephone1 = randomTelephones(1)
        val telephone2 = randomTelephones(1)
        val telephone3 = randomTelephones(1)

        val users = randomContacts(4, null).toMutableList()
        users[0] = users[0].copy(telephones = telephone1)
        users[1] = users[1].copy(telephones = telephone1)
        users[2] = users[2].copy(telephones = telephone2)
        users[3] = users[3].copy(telephones = telephone3)

        users.forEach { contactService.createContact(it) }

        val result = contactService.getContacts(0, 10, null, telephone1[0].number, null)

        assertEquals(result.content.size, 2)
        assertEquals(result.content.getOrNull(0)?.telephones?.map { it.number }, telephone1.map { it.number })
        assertEquals(result.content.getOrNull(1)?.telephones?.map { it.number }, telephone1.map { it.number })
    }

    @Test
    fun `get with email filter should retrieve 3 elements`() {
        val email1 = randomEmails(1)
        val email2 = randomEmails(1)
        val email3 = randomEmails(1)

        val users = randomContacts(5, null).toMutableList()
        users[0] = users[0].copy(emails = email1)
        users[1] = users[1].copy(emails = email1)
        users[2] = users[2].copy(emails = email2)
        users[3] = users[3].copy(emails = email3)
        users[4] = users[4].copy(emails = email1)

        users.forEach { contactService.createContact(it) }

        val result = contactService.getContacts(0, 10, email1[0].email, null, null)

        assertEquals(result.content.size, 3)
        assertEquals(result.content.getOrNull(0)?.emails?.map { it.email }, email1.map { it.email })
        assertEquals(result.content.getOrNull(1)?.emails?.map { it.email }, email1.map { it.email })
        assertEquals(result.content.getOrNull(2)?.emails?.map { it.email }, email1.map { it.email })
    }

    @Test
    fun `get with name filter should retrieve 2 elements`() {
        val name1 = "name1"
        val name2 = "name2"
        val name3 = "name3"

        val users = randomContacts(5, null).toMutableList()
        users[0] = users[0].copy(name = name1)
        users[1] = users[1].copy(name = name1)
        users[2] = users[2].copy(name = name2)
        users[3] = users[3].copy(name = name1)
        users[4] = users[4].copy(name = name3)

        users.forEach { contactService.createContact(it) }

        val result = contactService.getContacts(0, 10, null, null, name1)

        assertEquals(result.content.size, 3)
        assertEquals(result.content.getOrNull(0)?.name, "name1")
        assertEquals(result.content.getOrNull(1)?.name, "name1")
    }

    @Test
    fun `filter by name, telephone and email should return a list with a single element`() {
        val email = randomEmails(1)
        val telephone = randomTelephones(1)
        val address = randomAddresses(1)
        val name = UUID.randomUUID().toString()

        val contacts = randomContacts(10, 3).toMutableList()
        contacts[0] = contacts[0].copy(name = name, emails = email, telephones = telephone, addresses = address)

        contacts.forEach { contactService.createContact(it) }

        val result = contactService.getContacts(0, 10, email[0].email, telephone[0].number, name)

        assertEquals(1, result.content.size)
        assertEquals(contacts[0], CreateContactDTO.from(result.content[0]))
    }

    @Test
    fun `get a contact by id`() {
        val contactIds = contacts.map { contactService.createContact(it).id }

        contactIds.forEachIndexed { index, id ->
            val contact = contactService.getContactById(id)

            assertEquals(contacts[index], CreateContactDTO.from(contact))
        }
    }

    @Test
    fun `updating a contact should keep the same id`() {
        val contactDTO = contactService.createContact(contacts[0])

        val updatedContactDTO = contactService.updateContact(contactDTO.id, contacts[1])

        assertEquals(contactDTO.id, updatedContactDTO.id)
    }

    @Test
    fun `when removed a contact should not be retrieved`() {
        val addedContactDTO = contactService.createContact(contacts[0])
        contactService.deleteContactById(addedContactDTO.id)

        assertThrows<ContactException.NotFound>{ contactService.getContactById(addedContactDTO.id)}
    }

    @Test
    fun `should retrieve an email by emailId`() {
        val contactDTO = contactService.createContact(contacts[0])
        val emails = contactService.getContactEmails(contactDTO.id)

        val result = contactService.getContactEmailById(contactDTO.id, emails[1].id)

        assertEquals(emails[1].email, result.email)
    }

    @Test
    fun `get contact emails should return the emails list of a emails`() {
        val contacts = randomContacts(2, 3)
        val ids = mutableListOf<Long>()
        contacts.forEach { ids.add(contactService.createContact(it).id) }

        val result = contactService.getContactEmails(ids[1])

        assertEquals(result.size, 3)
    }

    @Test
    fun `updating an email with one that already exist should return the id of the latter`() {
        val email1 = randomEmails(1)
        val email2 = randomEmails(1)

        val ids = listOf(
            contacts[0].copy(emails = email1),
            contacts[1].copy(emails = email1 + email2)
        )
            .map { contactService.createContact(it).id }

        val emailIds = ids
            .map { contactService.getContactEmails(it) }
            .map { emails -> emails.map { it.id } }

        val updatedId = contactService.updateContactEmail(ids[0], emailIds[0][0], email2[0])

        assertEquals(emailIds[1][1], updatedId)
    }

    @Test
    fun `updating an email with an id not associated to that contact but that is already exist should return the id of the latter`() {
        val email1 = randomEmails(1)
        val email2 = randomEmails(1)

        val ids = listOf(
            contacts[0].copy(emails = email1),
            contacts[1].copy(emails = email1 + email2),
        )
            .map { contactService.createContact(it).id }

        val emailIds = ids
            .map { contactService.getContactEmails(it) }
            .map { emails -> emails.map { it.id } }

        val updatedId = contactService.updateContactEmail(ids[0], emailIds[1][1], email2[0])

        assertEquals(emailIds[1][1], updatedId)
    }

    @Test
    fun `updating an email that does not exist should create a new one`() {
        val contactDTO = contactService.createContact(contacts[0].copy(emails = listOf()))

        val updateId = contactService.updateContactEmail(contactDTO.id, 1, randomEmails(1).first())

        assertNotEquals(null, updateId)
    }

    @Test
    fun `updating an email for a contact should create a new one`() {
        val email1 = randomEmails(1)
        val email2 = randomEmails(1)

        val contactDTO = contactService.createContact(contacts[0].copy(emails = email1))
        val emailId = contactService.getContactEmails(contactDTO.id).first().id

        val updatedId = contactService.updateContactEmail(contactDTO.id, emailId, email2[0])

        assertNotEquals(null, updatedId)
        assertNotEquals(emailId, updatedId)
    }

    @Test
    fun `updating an email to the same value should return null`() {
        val email = randomEmails(1)
        val contactDTO = contactService.createContact(contacts[0].copy(emails = email))
        val emailId = contactService.getContactEmails(contactDTO.id).first().id

        val updatedId = contactService.updateContactEmail(contactDTO.id, emailId, email[0])

        assertEquals(null, updatedId)
    }

    @Test
    fun `should delete an email given contactId and emailId`() {

        val emails = randomEmails(3)
        val contacts = randomContacts(1, null)
        val contactDTO = contactService.createContact(contacts[0])

        val emailIds = mutableListOf<Long>()
        emails.forEach { emailIds.add(contactService.createContactEmail(contactDTO.id, it)) }

        contactService.deleteContactEmailById(contactDTO.id, emailIds[0])

        val result = contactService.getContactEmails(contactDTO.id)

        assertEquals(result.size, 2)
        assertEquals(result[0].email, emails[1].email)
        assertEquals(result[1].email, emails[2].email)
    }

    @Test
    fun `should retrieve all telephone numbers of a given contactId`() {
        val contactDTO = contactService.createContact(contacts[0])

        val result = contactService.getContactTelephones(contactDTO.id)

        assertEquals(6, result.size)
    }

    @Test
    fun `should retrieve a telephone by telephoneId`() {
        val contactDTO = contactService.createContact(contacts[0])
        val telephones = contactService.getContactTelephones(contactDTO.id)

        val result = contactService.getContactTelephoneById(contactDTO.id, telephones[1].id)

        assertEquals(telephones[1].number, result.number)
    }

    @Test
    fun `should create a new telephone and assign it to an existent contact`() {
        val telephones = randomTelephones(1)
        val contactDTO = contactService.createContact(contacts[0])

        val telephoneId = contactService.createContactTelephone(contactDTO.id, telephones[0])


        val result = contactService.getContactTelephones(contactDTO.id)

        assertEquals(7, result.size)
        assertThat(result.map { it.id }.toSet()).contains(telephoneId)
        assertThat(result.map { it.number }.toSet()).contains(telephones[0].number)
    }

    @Test
    fun `updating a telephone that does not exist should create a new one`() {
        val contactDTO = contactService.createContact(contacts[0].copy(telephones = listOf()))

        val updateId = contactService.updateContactTelephone(contactDTO.id, 1, randomTelephones(1).first())

        assertNotEquals(null, updateId)
    }

    @Test
    fun `updating a telephone for a contact should create a new one`() {
        val telephone1 = randomTelephones(1)
        val telephone2 = randomTelephones(1)

        val contactDTO = contactService.createContact(contacts[0].copy(telephones = telephone1))
        val telephoneId = contactService.getContactTelephones(contactDTO.id).first().id

        val updatedId = contactService.updateContactTelephone(contactDTO.id, telephoneId, telephone2[0])

        assertNotEquals(null, updatedId)
        assertNotEquals(telephoneId, updatedId)
    }

    @Test
    fun `updating a telephone to the same value should return null`() {
        val telephone = randomTelephones(1)
        val contactDTO = contactService.createContact(contacts[0].copy(telephones = telephone))
        val telephoneId = contactService.getContactTelephones(contactDTO.id).first().id

        val updatedId = contactService.updateContactTelephone(contactDTO.id, telephoneId, telephone[0])

        assertEquals(null, updatedId)
    }

    @Test
    fun `should delete a telephone given contactId and telephoneId`() {
        val telephones = randomTelephones(3)
        val contacts = randomContacts(1, null)
        val contactDTO = contactService.createContact(contacts[0])

        val telephonesIds = mutableListOf<Long>()
        telephones.forEach { telephonesIds.add(contactService.createContactTelephone(contactDTO.id, it)) }

        contactService.deleteContactTelephoneById(contactDTO.id, telephonesIds[0])

        val result = contactService.getContactTelephones(contactDTO.id)

        assertEquals(2, result.size)
        assertEquals(telephones[1].number, result[0].number)
        assertEquals(telephones[2].number, result[1].number)
    }

    @Test
    fun `should retrieve all the addresses of a given contactId`() {
        val contactDTO = contactService.createContact(contacts[0])

        val result = contactService.getContactAddresses(contactDTO.id)

        assertEquals(6, result.size)
    }

    @Test
    fun `should retrieve an address by addressId`() {
        val contactDTO = contactService.createContact(contacts[0])
        val addresses = contactService.getContactAddresses(contactDTO.id)

        val result = contactService.getContactAddressById(contactDTO.id, addresses[1].id)

        assertThat(result).usingRecursiveComparison().ignoringFields("id").isEqualTo(addresses[1])
    }

    @Test
    fun `should create a new address and assign it to an existent contact`() {
        val addresses = randomAddresses(1)
        val contactDTO = contactService.createContact(contacts[0])

        val addressId = contactService.createContactAddress(contactDTO.id, addresses[0])

        val result = contactService.getContactAddresses(contactDTO.id)

        assertEquals(7, result.size)
        assertThat(result.map { it.id }.toSet()).contains(addressId)
        assertThat(result.map { CreateAddressDTO.from(it) }.toSet()).contains(addresses[0])
    }

    @Test
    fun `updating a address that does not exist should create a new one`() {
        val contactDTO = contactService.createContact(contacts[0].copy(addresses = listOf()))

        val updateId = contactService.updateContactAddress(contactDTO.id, 1, randomAddresses(1).first())

        assertNotEquals(null, updateId)
    }

    @Test
    fun `updating an address for a contact should create a new one`() {
        val address1 = randomAddresses(1)
        val address2 = randomAddresses(1)

        val contactDTO = contactService.createContact(contacts[0].copy(addresses = address1))
        val addressId = contactService.getContactAddresses(contactDTO.id).first().id

        val updatedId = contactService.updateContactAddress(contactDTO.id, addressId, address2[0])

        assertNotEquals(null, updatedId)
        assertNotEquals(addressId, updatedId)
    }

    @Test
    fun `updating an address to the same value should return null`() {
        val address = randomAddresses(1)
        val contactDTO = contactService.createContact(contacts[0].copy(addresses = address))
        val addressId = contactService.getContactAddresses(contactDTO.id).first().id

        val updatedId = contactService.updateContactAddress(contactDTO.id, addressId, address[0])

        assertEquals(null, updatedId)
    }

    @Test
    fun `should delete an address given contactId and addressId`() {
        val addresses = randomAddresses(3)
        val contacts = randomContacts(1, null)
        val contactDTO = contactService.createContact(contacts[0])

        val addressIds = mutableListOf<Long>()
        addresses.forEach { addressIds.add(contactService.createContactAddress(contactDTO.id, it)) }

        contactService.deleteContactAddressById(contactDTO.id, addressIds[0])

        val result = contactService.getContactAddresses(contactDTO.id)

        assertEquals(2, result.size)

        assertThat(result[0]).usingRecursiveComparison().ignoringFields("id").isEqualTo(addresses[1])
        assertThat(result[1]).usingRecursiveComparison().ignoringFields("id").isEqualTo(addresses[2])
    }

}