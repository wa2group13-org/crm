package it.polito.wa2.g13.crm.utils

import it.polito.wa2.g13.crm.dtos.CreateCustomerDTO
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.joboffer.JobOfferStatus
import it.polito.wa2.g13.crm.data.message.Priority
import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.*
import java.util.*
import kotlin.random.Random

fun randomEmail(): String = "${UUID.randomUUID()}@gmail.com"

fun randomEmails(n: Int): List<CreateEmailDTO> =
    generateSequence { randomEmail() }.take(n).map { CreateEmailDTO(it) }.toList()


fun randomTelephone(): String = Random.nextLong(0, 999_999_9999).toString().padStart(10, '0')

fun randomTelephones(n: Int): List<CreateTelephoneDTO> =
    generateSequence { randomTelephone() }.take(n).map { CreateTelephoneDTO(it) }.toList()

fun randomAddress(): CreateAddressDTO = CreateAddressDTO(
    civic = UUID.randomUUID().toString(),
    street = UUID.randomUUID().toString(),
    city = UUID.randomUUID().toString(),
    postalCode = UUID.randomUUID().toString(),
)

fun randomAddresses(n: Int): List<CreateAddressDTO> = generateSequence { randomAddress() }.take(n).toList()

fun randomContact(
    telephones: List<CreateTelephoneDTO> = listOf(),
    emails: List<CreateEmailDTO> = listOf(),
    addresses: List<CreateAddressDTO> = listOf()
): CreateContactDTO = CreateContactDTO(
    name = UUID.randomUUID().toString(),
    surname = UUID.randomUUID().toString(),
    category = ContactCategory.entries.toTypedArray().random(),
    ssn = UUID.randomUUID().toString(),
    telephones = telephones,
    emails = emails,
    addresses = addresses,
)

fun randomContacts(n: Int, randomRelations: Int?) = generateSequence {
    if (randomRelations != null) randomContact(
        randomTelephones(randomRelations), randomEmails(randomRelations), randomAddresses(randomRelations)
    ) else randomContact()
}.take(n).toList()

fun randomCategorizedContact(
    telephones: List<CreateTelephoneDTO> = listOf(),
    emails: List<CreateEmailDTO> = listOf(),
    addresses: List<CreateAddressDTO> = listOf(),
    category: ContactCategory? = ContactCategory.entries.toTypedArray().random(),
): CreateContactDTO = CreateContactDTO(
    name = UUID.randomUUID().toString(),
    surname = UUID.randomUUID().toString(),
    category = category ?: ContactCategory.entries.toTypedArray().random(),
    ssn = UUID.randomUUID().toString(),
    telephones = telephones,
    emails = emails,
    addresses = addresses,
)

fun randomCategorizedContacts(
    n: Int,
    randomRelations: Int?,
    category: ContactCategory? = ContactCategory.entries.toTypedArray().random()
) = generateSequence {
    if (randomRelations != null) randomCategorizedContact(
        randomTelephones(randomRelations), randomEmails(randomRelations), randomAddresses(randomRelations), category
    ) else randomCategorizedContact()
}.take(n).toList()

fun randomMessage(priority: Priority?, channel: String?): CreateMessageDTO {
    val prior = priority ?: listOf(
        Priority.Low, Priority.Medium, Priority.High
    ).random()

    return CreateMessageDTO(
        "sender",
        channel ?: "channel",
        prior,
        "subject",
        "body",
        UUID.randomUUID().toString(),
    )
}

fun randomMessages(n: Int) = generateSequence {
    randomMessage(null, null)
}.take(n).toList()

fun randomProfessional(contactId: Long, randomRelations: Int?): CreateProfessionalDTO = CreateProfessionalDTO(
    notes = UUID.randomUUID().toString(),
    skills = randomRelations?.let { (0..it).map { CreateSkillDTO.from(UUID.randomUUID().toString()) }.toSet() }
        ?: setOf(),
    employmentState = EmploymentState.entries.toTypedArray().random(),
    dailyRate = Random.nextDouble(0.0, 1e10),
    contactId = contactId,
    contactInfo = null,
)

@Suppress("unused")
fun randomProfessionals(contactIds: List<Long>, randomRelations: Int?): List<CreateProfessionalDTO> = contactIds
    .map { randomProfessional(it, randomRelations) }
    .toList()

fun randomCustomer(contact: ContactDTO/*, randomRelations: Int?*/): CreateCustomerDTO = CreateCustomerDTO(
    note = UUID.randomUUID().toString(),
    contactId = contact.id,
    contactInfo = null,
)

fun randomCustomerWithContact(contact: CreateContactDTO): CreateCustomerDTO = CreateCustomerDTO(
    note = UUID.randomUUID().toString(),
    contactId = 0,
    contactInfo = contact,
)

@Suppress("unused")
fun randomCustomers(contacts: List<ContactDTO>/*, randomRelations: Int?*/): List<CreateCustomerDTO> = contacts
    .map { randomCustomer(it/*, randomRelations*/) }
    .toList()

fun randomSkills(n: Int): List<CreateSkillDTO> {
    return n.let {
        (0..it).map {
            CreateSkillDTO(UUID.randomUUID().toString())
        }
    }.toList()
}

fun randomJobOffer(
    customerId: Long,
    randomRelations: Int?,
    status: JobOfferStatus?
): CreateJobOfferDTO =
    CreateJobOfferDTO(
        customerId = customerId,
        description = UUID.randomUUID().toString(),
        status = status ?: JobOfferStatus.entries.toTypedArray().random(),
        skills = randomRelations?.let {
            (0..it).map { CreateSkillDTO(UUID.randomUUID().toString()) }.toSet()
        }
            ?: setOf(),
        duration = Random.nextLong(0, 100)
    )

fun randomJobOffers(ids: List<Long>, randomRelations: Int?, status: JobOfferStatus?): MutableList<CreateJobOfferDTO> =
    ids
        .map { randomJobOffer(it, randomRelations, status) }
        .toMutableList()