package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.contact.Email
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EmailRepository : JpaRepository<Email, Long> {
    fun findByEmail(email: String): Email?

    @Query("select e from Email e where e.email in :emails")
    fun findAllByEmails(emails: List<String>): List<Email>

    @Query("select e from Email e join e.contacts c where e.id=:id and c.id=:contactId")
    fun findByIdAndContactId(id: Long, contactId: Long): Email?
}