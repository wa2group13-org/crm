package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.message.Priority
import it.polito.wa2.g13.crm.data.message.Status
import it.polito.wa2.g13.crm.dtos.CreateMessageDTO
import it.polito.wa2.g13.crm.dtos.MessageActionsHistoryDTO
import it.polito.wa2.g13.crm.dtos.MessageDTO
import it.polito.wa2.g13.crm.exceptions.MessageException
import it.polito.wa2.g13.crm.repositories.SortBy
import org.springframework.data.domain.Page

interface MessageService {

    fun listMessages(
        page: Int?,
        limit: Int?,
        sortBy: SortBy?,
        filterByState: Status?
    ): Page<MessageDTO>

    fun createMessage(
        createMessageDTO: CreateMessageDTO
    ): MessageDTO

    @Throws(MessageException.NotFound::class)
    fun getMessage(
        messageId: Long
    ): MessageDTO

    @Throws(MessageException.NotFound::class)
    fun changeMessageState(
        messageId: Long,
        status: Status,
        comment: String?
    ): MessageDTO

    @Throws(MessageException.NotFound::class)
    fun changeMessagePriority(
        messageId: Long,
        priority: Priority
    ): MessageDTO

    @Throws(MessageException.NotFound::class)
    fun getMessageHistory(
        messageId: Long,
    ): List<MessageActionsHistoryDTO>

    @Throws(MessageException.NotFound::class)
    fun getMessageByMailId(mailId: String): MessageDTO
}