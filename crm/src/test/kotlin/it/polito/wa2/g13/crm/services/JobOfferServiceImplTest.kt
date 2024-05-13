package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.dtos.CreateJobOfferDTO
import it.polito.wa2.g13.crm.dtos.JobOfferFilters
import it.polito.wa2.g13.crm.dtos.UpdateJobOfferStatusDTO
import it.polito.wa2.g13.crm.utils.randomCategorizedContacts
import it.polito.wa2.g13.crm.utils.randomCustomers
import it.polito.wa2.g13.crm.utils.randomJobOffers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

    //    @Qualifier("customerService")
    @Autowired
    lateinit var customerService: CustomerService

    @Autowired
    lateinit var jobOfferService: JobOfferService

    fun initJobOffers(n: Int): Pair<List<Long>, List<CreateJobOfferDTO>> {
        val contacts = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsIds = contacts.map { contactService.createContact(it) }.toList()
        randomCustomers(contactService.getContacts(0, 10, null, null, null))
        val customersIds = contactsIds.map { customerService.createCustomer(it).id }.toList()
        val jobOffers = randomJobOffers(customersIds, n, JobOfferStatus.Created)
        val jobOffersIds = jobOffers.map { jobOfferService.createJobOffer(it).id }.toList()

        return Pair(jobOffersIds, jobOffers)
    }

    @Test
    fun `should retrieve all job offers`() {
        val (_, _) = initJobOffers(7)

        val res = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(7, res.size)
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

        assertEquals(3, result.size)
        updated.forEachIndexed { id, jobOfferDTO ->
            assertThat(result[id]).usingRecursiveComparison().ignoringFields("id").isEqualTo(jobOfferDTO)
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


}