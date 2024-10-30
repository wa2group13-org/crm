package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.customer.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface CustomerRepository : JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer>