package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.ContactIdDTO
import it.polito.wa2.g13.crm.dtos.CustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerNoteDTO
import it.polito.wa2.g13.crm.services.CustomerService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/customers")
@Validated
class CustomerController(private val customerService: CustomerService) {

    @GetMapping("", "/")
    @ResponseStatus(HttpStatus.OK)
    fun getCustomers(
        @RequestParam("page") page: Int,
        @RequestParam("limit") limit: Int,
    ): Page<CustomerDTO> {
        return customerService.getCustomers(page, limit)
    }

    @GetMapping("/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    fun getCustomerById(@PathVariable("customerId") customerId: Long): CustomerDTO {
        return customerService.getCustomerById(customerId)
    }

    @PostMapping("", "/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@RequestBody contactIdDTO: ContactIdDTO): CustomerDTO {
        return customerService.createCustomer(contactIdDTO.contactId)
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable("customerId") customerId: Long) {
        customerService.deleteCustomerById(customerId)
    }

    @PutMapping("/{customerId}/note")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomerNote(@PathVariable customerId: Long, @RequestBody customerNoteDTO: CustomerNoteDTO) {
        customerService.updateCustomerNote(customerId, customerNoteDTO.note)
    }

    @PutMapping("/{customerId}/contact")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomerContact(@PathVariable customerId: Long, @RequestBody contactIdDTO: ContactIdDTO) {
        customerService.updateCustomerContact(customerId, contactIdDTO.contactId)
    }


}