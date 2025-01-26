package it.polito.wa2.g13.crm.repositories

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.message.Message
import it.polito.wa2.g13.crm.data.message.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy::class)
enum class SortBy {
    PriorityAsc,
    PriorityDesc,
    StatusAsc,
    StatusDesc,
    DateAsc,
    DateDesc,
}

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    fun findAllByStatus(page: Pageable, status: Status): Page<Message>

    fun findFirstBySenderAndChannel(sender: String, channel: String): Message?

    fun findByMailId(mailId: String): Message?

    fun findByContact(contact: Contact, pageable: Pageable): Page<Message>
}