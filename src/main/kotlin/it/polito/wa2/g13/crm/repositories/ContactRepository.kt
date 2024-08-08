package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.contact.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact>