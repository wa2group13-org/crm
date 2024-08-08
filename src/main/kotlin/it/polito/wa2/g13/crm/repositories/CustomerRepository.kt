package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.customer.Customer
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, Long> {

}