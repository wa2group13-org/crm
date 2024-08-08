package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.message.Message
import it.polito.wa2.g13.crm.data.message.Priority
import it.polito.wa2.g13.crm.data.message.Status
import java.time.OffsetDateTime

data class MessageDTO(
    val id: Long,
    val body: String?,
    val sender: String,
    val date: OffsetDateTime,
    val subject: String?,
    val channel: String,
    val priority: Priority,
    val status: Status,
    val mailId: String?,
) {
    companion object {
        @JvmStatic
        fun from(message: Message): MessageDTO {
            return MessageDTO(
                id = message.id,
                body = message.body,
                sender = message.sender,
                date = message.date,
                subject = message.subject,
                channel = message.channel,
                priority = message.priority,
                status = message.status,
                mailId = message.mailId,
            )
        }
    }
}

