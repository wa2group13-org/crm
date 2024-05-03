package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.services.ContactService
import it.polito.wa2.g13.crm.utils.requestURIDropLast
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import kotlin.jvm.Throws

@RestController
@RequestMapping("/API/contacts")
@Validated
class ContactController(
    private val contactService: ContactService,
) {

    @GetMapping("", "/")
    fun getContacts(
        @RequestParam("page") page: Int,
        @RequestParam("limit") limit: Int,
        @RequestParam("byEmail") byEmail: String?,
        @RequestParam("byTelephone") byTelephone: String?,
        @RequestParam("byName") byName: String?,
    ): List<ContactDTO> {
        return contactService.getContacts(page, limit, byEmail, byTelephone, byName)
    }

    @PostMapping("", "/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createContact(
        request: HttpServletRequest,
        @RequestBody @Valid contact: CreateContactDTO,
    ): ResponseEntity<Unit> {
        val id = contactService.createContact(contact)

        return ResponseEntity.created(URI.create("${request.requestURI}/$id")).build()
    }

    @GetMapping("/{contactId}")
    @Throws(ContactException.NotFound::class)
    fun getContactById(@PathVariable("contactId") id: Long): ContactDTO {
        return contactService.getContactById(id)
    }

    @PutMapping("/{contactId}")
    fun updateContact(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @Valid @RequestBody contact: CreateContactDTO
    ): ResponseEntity<Unit> {
        val newId = contactService.updateContact(contactId, contact)

        return if (newId != null) {
            val contactURI = URI.create("${request.requestURIDropLast(1)}/$newId")
            ResponseEntity.created(contactURI).build()
        } else {
            ResponseEntity.noContent().build()
        }
    }


    @DeleteMapping("/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Throws(ContactException.NotFound::class)
    fun deleteContactById(@PathVariable("contactId") id: Long) {
        contactService.deleteContactById(id)
    }


    @GetMapping("/{contactId}/emails")
    fun getContactEmails(@PathVariable("contactId") contactId: Long): List<EmailDTO> {
        return contactService.getContactEmails(contactId)
    }

    @PostMapping("/{contactId}/emails")
    @ResponseStatus(HttpStatus.CREATED)
    @Throws(ContactException::class)
    fun createContactEmail(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @Valid @RequestBody email: CreateEmailDTO
    ): ResponseEntity<Unit> {
        val emailId = contactService.createContactEmail(contactId, email)

        return ResponseEntity.created(URI.create("${request.requestURI}/$emailId")).build()
    }

    @GetMapping("/{contactId}/emails/{emailId}")
    @Throws(ContactException::class)
    fun getContactEmailById(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("emailId") emailId: Long,
    ): EmailDTO {
        return contactService.getContactEmailById(contactId, emailId)
    }


    @PutMapping("/{contactId}/emails/{emailId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateContactEmailById(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @PathVariable("emailId") emailId: Long,
        @Valid @RequestBody email: CreateEmailDTO,
    ): ResponseEntity<Unit> {
        val returnId = contactService.updateContactEmail(contactId, emailId, email)

        return if (returnId != null) {
            val emailsURI = URI.create("${request.requestURIDropLast(1)}/$returnId")
            ResponseEntity.created(emailsURI).build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/{contactId}/emails/{emailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteContactEmailById(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("emailId") emailId: Long,
    ) {
        contactService.deleteContactEmailById(contactId, emailId)
    }

    @GetMapping("/{contactId}/telephones")
    fun getContactTelephones(@PathVariable("contactId") contactId: Long): List<TelephoneDTO> {
        return contactService.getContactTelephones(contactId)
    }

    @PostMapping("/{contactId}/telephones")
    @ResponseStatus(HttpStatus.CREATED)
    fun createContactTelephone(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @Valid @RequestBody telephone: CreateTelephoneDTO,
    ): ResponseEntity<Unit> {
        val telephoneId = contactService.createContactTelephone(contactId, telephone)

        return ResponseEntity.created(URI.create("${request.requestURI}/$telephoneId")).build()
    }

    @GetMapping("/{contactId}/telephones/{telephoneId}")
    @Throws(ContactException::class)
    fun getContactTelephoneById(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("telephoneId") telephoneId: Long,
    ): TelephoneDTO {
        return contactService.getContactTelephoneById(contactId, telephoneId)
    }

    @PutMapping("/{contactId}/telephones/{telephoneId}")
    fun updateContactTelephone(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @PathVariable("telephoneId") telephoneId: Long,
        @Valid @RequestBody telephone: CreateTelephoneDTO,
    ): ResponseEntity<Unit> {
        val returnId = contactService.updateContactTelephone(contactId, telephoneId, telephone)

        return if (returnId != null) {
            val telephoneNewURI = URI.create("${request.requestURIDropLast(1)}/$returnId")
            ResponseEntity.created(telephoneNewURI).build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/{contactId}/telephones/{telephoneId}")
    @Throws(ContactException::class)
    fun deleteContactTelephoneById(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("telephoneId") telephoneId: Long,
    ) {
        return contactService.deleteContactTelephoneById(contactId, telephoneId)
    }

    @GetMapping("/{contactId}/addresses")
    fun getContactAddresses(@PathVariable("contactId") contactId: Long): List<AddressDTO> {
        return contactService.getContactAddresses(contactId)
    }

    @PostMapping("/{contactId}/addresses")
    fun createContactAddress(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @Valid @RequestBody address: CreateAddressDTO
    ): ResponseEntity<Unit> {
        val addressId = contactService.createContactAddress(contactId, address)

        return ResponseEntity.created(URI.create("${request.requestURI}/$addressId")).build()
    }

    @GetMapping("/{contactId}/addresses/{addressId}")
    @Throws(ContactException::class)
    fun getContactAddressById(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("addressId") addressId: Long
    ): AddressDTO {
        return contactService.getContactAddressById(contactId, addressId)
    }

    @PutMapping("/{contactId}/addresses/{addressId}")
    fun updateContactAddress(
        request: HttpServletRequest,
        @PathVariable("contactId") contactId: Long,
        @PathVariable("addressId") addressId: Long,
        @Valid @RequestBody address: CreateAddressDTO
    ): ResponseEntity<Unit> {
        val returnId = contactService.updateContactAddress(contactId, addressId, address)

        return if (returnId != null) {
            val addressNewURI = URI.create("${request.requestURI.dropLast(1)}/$returnId")
            ResponseEntity.created(addressNewURI).build()
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @DeleteMapping("/{contactId}/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Throws(ContactException::class)
    fun deleteContactAddress(
        @PathVariable("contactId") contactId: Long,
        @PathVariable("addressId") addressId: Long,
    ) {
        contactService.deleteContactAddressById(contactId, addressId)
    }
}