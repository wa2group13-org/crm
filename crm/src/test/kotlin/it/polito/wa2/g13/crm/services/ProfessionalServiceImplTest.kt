package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.dtos.CreateContactDTO
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.LocationFilter
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.utils.assertRecursive
import it.polito.wa2.g13.crm.utils.randomContacts
import it.polito.wa2.g13.crm.utils.randomProfessional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class ProfessionalServiceImplTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(ProfessionalServiceImplTest::class.java)

        private val contacts = randomContacts(10, 5).map { it.copy(category = ContactCategory.Unknown) }
        private val _professionals = mutableListOf<CreateProfessionalDTO>()
        private val professionals: List<CreateProfessionalDTO> = _professionals
        private val _professionalsId = mutableListOf<Long>()
        private val professionalsId: List<Long> = _professionalsId

        @BeforeAll
        @JvmStatic
        fun createDb(
            @Autowired contactService: ContactService,
            @Autowired professionalService: ProfessionalService,
        ) {
            contacts.forEach { contact ->
                val contactDTO = contactService.createContact(contact)

                val professional = randomProfessional(contactDTO.id, 5)
                _professionals.add(professional)

                val professionalId = professionalService.createProfessional(professional).id
                _professionalsId.add(professionalId)
            }

            logger.info("Initialized DB \uD83E\uDD11\uD83E\uDD11")
        }
    }

    @Autowired
    lateinit var contactService: ContactService

    @Autowired
    lateinit var professionalService: ProfessionalService

    private fun newProfessional(): Pair<CreateProfessionalDTO, CreateContactDTO> {
        val contact = randomContacts(1, 5)[0].copy(category = ContactCategory.Unknown)
        val contactDTO = contactService.createContact(contact)
        return Pair(randomProfessional(contactDTO.id, 5), contact)
    }

    @Test
    fun `creating a new professional should modify also the contact`() {
        val (professional, _) = newProfessional()

        val professionalId = professionalService.createProfessional(professional).id

        val p = professionalService.getProfessional(professionalId)
        val c = contactService.getContactById(p.contact.id)

        assertEquals(ContactCategory.Professional, p.contact.category)
        assertRecursive(c, p.contact)
    }

    @Test
    fun `deleting a professional should not delete its contact`() {
        val professional = professionals.first()
        val id = professionalsId.first()

        professionalService.deleteProfessional(id)

        val contact = assertDoesNotThrow { contactService.getContactById(professional.contactId) }
        assertEquals(ContactCategory.Unknown, contact.category)
    }

    @Test
    fun `deleting a contact should delete the associated professional`() {
        val professional = professionals.first()
        val professionalId = professionalsId.first()

        contactService.deleteContactById(professional.contactId)

        val ex = assertThrows<ProfessionalException> { professionalService.getProfessional(professionalId) }
        assertEquals(ProfessionalException.NotFound::class, ex::class)
    }

    @Test
    fun `getting by filtering for the first professional should return only it`() {
        val professionalToFilter = professionals.first()
        val address = contacts.first().addresses.first()

        val gotProfessional = professionalService.getProfessionals(
            0, 10, ProfessionalFilters(
                bySkills = professionalToFilter.skills.map { it.skill }.toSet(),
                byEmploymentState = professionalToFilter.employmentState,
                byLocation = LocationFilter(
                    byPostalCode = address.postalCode,
                    byCity = address.city,
                    byStreet = address.street,
                    byCivic = address.civic,
                )
            )
        )

        assertEquals(1, gotProfessional.content.size)
        assertRecursive(professionalToFilter, CreateProfessionalDTO.from(gotProfessional.content[0]))
    }

    @Test
    fun `filtering by wrong location should fail`() {
        val professionalToFilter = professionals.first()
        val address = contacts[1].addresses.first()

        val gotProfessional = professionalService.getProfessionals(
            0, 10, ProfessionalFilters(
                bySkills = professionalToFilter.skills.map { it.skill }.toSet(),
                byEmploymentState = professionalToFilter.employmentState,
                byLocation = LocationFilter(
                    byPostalCode = address.postalCode,
                    byCity = address.city,
                    byStreet = address.street,
                    byCivic = address.civic,
                )
            )
        )

        assertEquals(0, gotProfessional.content.size)
    }
}