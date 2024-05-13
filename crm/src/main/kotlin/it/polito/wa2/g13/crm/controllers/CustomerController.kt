package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.ContactIdDTO
import it.polito.wa2.g13.crm.dtos.CustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerNoteDTO
import it.polito.wa2.g13.crm.services.CustomerService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/customers")
@Validated
class CustomerController(private val customerService: CustomerService) {

    @GetMapping("", "/")
    fun getCustomers(
        @RequestParam("page") page: Int,
        @RequestParam("limit") limit: Int,
    ): List<CustomerDTO> {
        return customerService.getCustomers(page, limit)
    }

    @GetMapping("/{customerId}")
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
    fun updateCustomerNote(@PathVariable customerId: Long, @RequestBody customerNoteDTO: CustomerNoteDTO) {
        customerService.updateCustomerNote(customerId, customerNoteDTO.note)
    }

    @PutMapping("/{customerId}/contact")
    fun updateCustomerContact(@PathVariable customerId: Long, @RequestBody contactIdDTO: ContactIdDTO) {
        customerService.updateCustomerContact(customerId, contactIdDTO.contactId)
    }


}