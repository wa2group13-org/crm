package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.CreateCustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerFilters
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
interface CustomerService {
    fun getCustomers(page: Int, limit: Int, customerFilters: CustomerFilters): Page<CustomerDTO>

    fun getCustomerById(id: Long): CustomerDTO

    fun createCustomer(customerDto: CreateCustomerDTO): CustomerDTO

    fun deleteCustomerById(customerId: Long)

    fun updateCustomerContact(customerId: Long, contactId: Long)

    fun updateCustomerNote(customerId: Long, note: String?)

}