package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.message.MessageActionsHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageActionsHistoryRepository : JpaRepository<MessageActionsHistory, Long> {
    fun findAllByMessageId(messageId: Long): List<MessageActionsHistory>
}