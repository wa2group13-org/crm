package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.CustomerDTO
import org.springframework.stereotype.Service

@Service
interface CustomerService {
    fun getCustomers(page: Int, limit: Int) : List<CustomerDTO>

    fun getCustomerById(id: Long) : CustomerDTO

    fun createCustomer(contactId: Long) : CustomerDTO

    fun deleteCustomerById(customerId: Long)

    fun updateCustomerContact(customerId: Long, contactId: Long)

    fun updateCustomerNote(customerId: Long, note: String?)

}