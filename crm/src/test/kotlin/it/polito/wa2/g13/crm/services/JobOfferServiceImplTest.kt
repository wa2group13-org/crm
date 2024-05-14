package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.dtos.CreateJobOfferDTO
import it.polito.wa2.g13.crm.dtos.JobOfferFilters
import it.polito.wa2.g13.crm.dtos.UpdateJobOfferDetailsDTO
import it.polito.wa2.g13.crm.dtos.UpdateJobOfferStatusDTO
import it.polito.wa2.g13.crm.utils.*
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

    @Autowired
    lateinit var professionalService: ProfessionalService

    fun initJobOffers(n: Int): Pair<MutableList<Long>, MutableList<CreateJobOfferDTO>> {
        val contacts = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsIds = contacts.map { contactService.createContact(it) }.toList()
        randomCustomers(contactService.getContacts(0, 10, null, null, null).toList())
        val customersIds = contactsIds.map { customerService.createCustomer(it).id }.toMutableList()
        val jobOffers = randomJobOffers(customersIds, n, JobOfferStatus.Created)
        val jobOffersIds = jobOffers.map { jobOfferService.createJobOffer(it).id }.toMutableList()

        return Pair(jobOffersIds, jobOffers)
    }

    fun initConsolidatedJobOffers(n: Int): Pair<MutableList<Long>, MutableList<CreateJobOfferDTO>> {
        val contactsC = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsCIds = contactsC.map { contactService.createContact(it) }.toList()
        val contactsP = randomCategorizedContacts(n, n, ContactCategory.Unknown)
        val contactsPIds = contactsP.map { contactService.createContact(it) }.toList()

        val customerIds = contactsCIds.map { customerService.createCustomer(it).id }
        val professionalIds = contactsPIds.map { professionalService.createProfessional(randomProfessional(it, n)) }

        val jobOffers = randomJobOffers(customerIds, n, JobOfferStatus.CandidateProposal)
        val jobOffersIds = jobOffers.map { jobOfferService.createJobOffer(it).id }.toMutableList()

        jobOffersIds.zip(professionalIds).forEach {
            jobOfferService.updateJobOfferStatus(
                it.first,
                UpdateJobOfferStatusDTO(JobOfferStatus.Consolidated, it.second, "Candidate Consolidated!")
            )
        }

        val jobOfferDTOs =
            jobOfferService.getJobOffersByParams(null, 0, n).toList().map { CreateJobOfferDTO.from(it) }.toMutableList()

        return Pair(jobOffersIds, jobOfferDTOs)
    }


    @Test
    fun `should retrieve all job offers`() {
        val (_, _) = initJobOffers(7)

        val res = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(7, res.content.size)
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

        assertEquals(3, result.content.size)
        updated.forEachIndexed { id, jobOfferDTO ->
            assertThat(result.content[id]).usingRecursiveComparison().ignoringFields("id").isEqualTo(jobOfferDTO)
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

    @Test
    fun `should create a new job offer`() {
        val (ids, jobOffers) = initJobOffers(5)
        val contact = randomCategorizedContact(category = ContactCategory.Unknown)
        val contactId = contactService.createContact(contact)
        randomCustomer(contactService.getContactById(contactId))
        val customer = customerService.createCustomer(contactId)
        val newJobOffer = randomJobOffer(customer.id, null, null)

        val newJobOfferDTO = jobOfferService.createJobOffer(newJobOffer)
        jobOffers.add(newJobOffer)
        ids.add(newJobOfferDTO.id)

        val result = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(6, result.content.size)
        assertThat(result.content).contains(newJobOfferDTO)

    }

    @Test
    fun `should retrieve the value of a jobOffer given its id`() {
        val (ids, _) = initConsolidatedJobOffers(5)
        val randomId = ids.random()

        val jobOffer = jobOfferService.getJobOfferById(randomId)
        val result = jobOfferService.getJobOfferValue(randomId)

        assertEquals(jobOffer.value, result)
    }

    @Test
    fun `should update job offer details given its id`() {
        val (ids, _) = initJobOffers(5)
        val randomId = ids.random()

        val updateJobOfferDetailsDTO = UpdateJobOfferDetailsDTO("Modified", randomSkills(4).toSet(), 200)
        jobOfferService.updateJobOfferDetails(randomId, updateJobOfferDetailsDTO)

        val result = jobOfferService.getJobOfferById(randomId)
        assertEquals(updateJobOfferDetailsDTO.skills.map { it.skill }.toSet(), result.skills)
        assertEquals(updateJobOfferDetailsDTO.duration, result.duration)
        assertEquals(updateJobOfferDetailsDTO.description, result.description)

    }

    @Test
    fun `should update job offer status given its id`() {
        val (ids, _) = initJobOffers(2)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        val filter1 = setOf(JobOfferStatus.SelectionPhase)
        val result1 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter1), 0, 10)

        val updateJobOfferStatusDTO2 = UpdateJobOfferStatusDTO(JobOfferStatus.CandidateProposal, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO2)
        val filter2 = setOf(JobOfferStatus.CandidateProposal)
        val result2 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter2), 0, 10)


        assertEquals(updateJobOfferStatusDTO1.status, result1.content[0].status)
        assertEquals(updateJobOfferStatusDTO2.status, result2.content[0].status)
    }

    @Test
    fun `should go back to SelectionPhase from Consolidated status`() {
        val (ids, _) = initConsolidatedJobOffers(2)
        val randomId = ids.random()

        val updateJobOfferStatusDTO1 = UpdateJobOfferStatusDTO(JobOfferStatus.SelectionPhase, null, null)
        jobOfferService.updateJobOfferStatus(randomId, updateJobOfferStatusDTO1)
        val filter1 = setOf(JobOfferStatus.SelectionPhase)
        val result1 = jobOfferService.getJobOffersByParams(filters = JobOfferFilters(null, null, filter1), 0, 10)

        assertEquals(updateJobOfferStatusDTO1.status, result1.content[0].status)
    }

    @Test
    fun `should delete a job offer`() {
        val (ids, jobOffers) = initConsolidatedJobOffers(2)
        val randomId = ids.random()

        jobOfferService.deleteJobOffer(randomId)

        val result = jobOfferService.getJobOffersByParams(null, 0, 10)

        assertEquals(1, result.content.size)
        assertThat(CreateJobOfferDTO.from(result.content[0])).usingRecursiveComparison()
            .isEqualTo(jobOffers[randomId.toInt()])
    }

}