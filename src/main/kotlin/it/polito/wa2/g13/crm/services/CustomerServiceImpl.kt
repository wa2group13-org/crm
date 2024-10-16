package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.contact.ContactCategory
import it.polito.wa2.g13.crm.data.customer.Customer
import it.polito.wa2.g13.crm.dtos.CreateCustomerDTO
import it.polito.wa2.g13.crm.dtos.CustomerDTO
import it.polito.wa2.g13.crm.exceptions.ContactException
import it.polito.wa2.g13.crm.exceptions.CustomerException
import it.polito.wa2.g13.crm.repositories.ContactRepository
import it.polito.wa2.g13.crm.repositories.CustomerRepository
import it.polito.wa2.g13.crm.utils.nullable
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@Transactional
class CustomerServiceImpl(
    private val customerRepository: CustomerRepository,
    private val contactRepository: ContactRepository,
    private val contactService: ContactService,
) : CustomerService {
    override fun getCustomers(page: Int, limit: Int): Page<CustomerDTO> {
        return customerRepository.findAll(PageRequest.of(page, limit)).map { CustomerDTO.from(it) }
    }

    override fun getCustomerById(id: Long): CustomerDTO {
        return customerRepository.findById(id).map { CustomerDTO.from(it) }.nullable()
            ?: throw CustomerException.NotFound.from(id)
    }

    override fun createCustomer(customerDto: CreateCustomerDTO): CustomerDTO {
        val contact = if (customerDto.contactInfo == null) {
            contactRepository.findById(customerDto.contactId).nullable() ?: throw ContactException.NotFound.from(
                customerDto.contactId
            )
        } else {
            Contact.from(contactService.createContact(customerDto.contactInfo))
        }

        if (contact.category != ContactCategory.Unknown) throw CustomerException.ContactAlreadyTaken.from(
            customerDto.contactId
        )

        val customer = Customer.createNewCustomer(contact)
        contact.category = ContactCategory.Customer
        val newCustomer = customerRepository.save(customer)

        return CustomerDTO.from(newCustomer)
    }

    override fun deleteCustomerById(customerId: Long) {
        val customer =
            customerRepository.findById(customerId).nullable() ?: throw CustomerException.NotFound.from(customerId)
        customer.contact.category = ContactCategory.Unknown
        customerRepository.delete(customer)
    }

    override fun updateCustomerContact(customerId: Long, contactId: Long) {
        val customer =
            customerRepository.findById(customerId).nullable() ?: throw CustomerException.NotFound.from(customerId)
        val contact =
            contactRepository.findById(contactId).nullable() ?: throw ContactException.NotFound.from(contactId)
        if (contact.category != ContactCategory.Unknown) throw CustomerException.ContactAlreadyTaken.from(contactId)
        customer.contact = contact
        customerRepository.save(customer)
    }

    override fun updateCustomerNote(customerId: Long, note: String?) {
        val customer =
            customerRepository.findById(customerId).nullable() ?: throw CustomerException.NotFound.from(customerId)
        customer.note = note
    }


}