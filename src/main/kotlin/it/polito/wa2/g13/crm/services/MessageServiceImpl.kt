package it.polito.wa2.g13.crm.services


import it.polito.wa2.g13.crm.data.contact.Contact
import it.polito.wa2.g13.crm.data.message.*
import it.polito.wa2.g13.crm.dtos.CreateMessageDTO
import it.polito.wa2.g13.crm.dtos.MessageActionsHistoryDTO
import it.polito.wa2.g13.crm.dtos.MessageDTO
import it.polito.wa2.g13.crm.exceptions.MessageException
import it.polito.wa2.g13.crm.repositories.ContactRepository
import it.polito.wa2.g13.crm.repositories.MessageActionsHistoryRepository
import it.polito.wa2.g13.crm.repositories.MessageRepository
import it.polito.wa2.g13.crm.repositories.SortBy
import it.polito.wa2.g13.crm.utils.nullable
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val messageActionsHistoryRepository: MessageActionsHistoryRepository,
    private val contactRepository: ContactRepository,
) : MessageService {

    companion object {
        private val logger = LoggerFactory.getLogger(MessageService::class.java)
    }

    override fun listMessages(page: Int?, limit: Int?, sortBy: SortBy?, filterByState: Status?): Page<MessageDTO> {
        var pageable = PageRequest.of(page ?: 0, limit ?: 100)
        if (sortBy != null) {
            val sorting = when (sortBy) {
                SortBy.PriorityAsc -> Sort.by("priority").ascending()
                SortBy.PriorityDesc -> Sort.by("priority").descending()
                SortBy.StatusAsc -> Sort.by("status").ascending()
                SortBy.StatusDesc -> Sort.by("status").descending()
                SortBy.DateAsc -> Sort.by("priority").ascending()
                SortBy.DateDesc -> Sort.by("priority").descending()
            }
            pageable = pageable.withSort(sorting)
        }

        return if (filterByState != null) {
            messageRepository.findAllByStatus(pageable, filterByState).map { MessageDTO.from(it) }
        } else {
            messageRepository.findAll(
                pageable
            ).map {
                MessageDTO.from(it)
            }
        }
    }

    override fun createMessage(createMessageDTO: CreateMessageDTO): MessageDTO {
        /** find a previous message with the same sender and channel (if so, a contact already exists anonymous or not)
         * if a contact exists then a **/
        val previousMessage =
            messageRepository.countAllBySenderAndChannel(createMessageDTO.sender, createMessageDTO.channel)
        if (previousMessage == 0) {
            val newContact = Contact.createAnonymous(createMessageDTO.sender, createMessageDTO.channel)
            contactRepository.save(newContact)
        }

        val message = Message(
            body = createMessageDTO.body,
            sender = createMessageDTO.sender,
            date = OffsetDateTime.now(),
            subject = createMessageDTO.subject,
            channel = createMessageDTO.channel,
            priority = createMessageDTO.priority,
            status = Status.Received,
            history = mutableSetOf(),
            mailId = createMessageDTO.mailId
        )
        val logAction = MessageActionsHistory(message, message.status, message.date, null)
        message.history.add(logAction)

        messageActionsHistoryRepository.save(logAction)
        val messageDTO = MessageDTO.from(messageRepository.save(message))
        logger.info("Message with id ${messageDTO.id} created")
        return messageDTO
    }

    override fun getMessage(messageId: Long): MessageDTO {
        return MessageDTO.from(
            messageRepository.findById(messageId).nullable()
                ?: throw MessageException.NotFound("Message with id $messageId not found")
        )
    }

    override fun changeMessageState(messageId: Long, status: Status, comment: String?): MessageDTO {
        val message = messageRepository.findById(messageId).nullable()
            ?: throw MessageException.NotFound("Message with id $messageId not found")

        //get the event (action) that leads to the desired status
        val event = StateMachine(message.status).goTo(status)

        when (event) {
            Event.None ->
                throw MessageException.ForbiddenTransition(
                    "Illegal action: cannot change from ${message.status} to $status"
                )

            else -> message.status = status
        }

        //log change into history table
        val messageActionHistory = MessageActionsHistory(message, message.status, OffsetDateTime.now(), comment)
        message.history.add(messageActionHistory)
        messageRepository.save(message)
        logger.info("Status of message ${message.id} changed to ${message.status}")
        return MessageDTO.from(message)
    }

    override fun changeMessagePriority(messageId: Long, priority: Priority): MessageDTO {
        val message = messageRepository.findById(messageId).nullable()
            ?: throw MessageException.NotFound("Message with id $messageId not found")
        message.priority = priority
        return MessageDTO.from(messageRepository.save(message))
    }

    override fun getMessageHistory(messageId: Long): List<MessageActionsHistoryDTO> {
        val message = messageRepository.findById(messageId).nullable()
            ?: throw MessageException.NotFound("Message with id $messageId not found")

        return message.history.map { MessageActionsHistoryDTO.from(it) }
    }

    override fun getMessageByMailId(mailId: String): MessageDTO {
        return messageRepository.findByMailId(mailId)?.let { MessageDTO.from(it) }
            ?: throw MessageException.NotFound("Message with mailId $mailId was not found")
    }
}