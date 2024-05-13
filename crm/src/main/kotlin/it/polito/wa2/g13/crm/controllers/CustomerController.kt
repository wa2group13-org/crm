package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.CustomerDTO
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
    fun getCustomers(
        @RequestParam("page") page: Int,
        @RequestParam("limit") limit: Int,
    ): Page<CustomerDTO> {
        return customerService.getCustomers(page, limit)
    }

    @GetMapping("/{customerId}")
    fun getCustomerById(@PathVariable("customerId") customerId: Long): CustomerDTO {
        return customerService.getCustomerById(customerId)
    }

    @PostMapping("", "/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@RequestBody contactId: Long): CustomerDTO {
        return customerService.createCustomer(contactId)
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable("customerId") customerId: Long) {
        customerService.deleteCustomerById(customerId)
    }

    @PutMapping("/{customerId}/note")
    fun updateCustomerNote(@PathVariable customerId: Long, @RequestBody note: String?) {
        customerService.updateCustomerNote(customerId, note)
    }

    @PutMapping("/{customerId}/contact")
    fun updateCustomerContact(@PathVariable customerId: Long, @RequestBody contactId: Long) {
        customerService.updateCustomerContact(customerId, contactId)
    }


}