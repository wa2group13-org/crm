package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.message.MessageActionsHistory
import it.polito.wa2.g13.crm.data.message.Status
import java.time.OffsetDateTime

data class MessageActionsHistoryDTO(
    val id: Long,
    val messageId: Long,
    val status: Status,
    val timestamp: OffsetDateTime,
    val comment: String?,
) {
    companion object {
        @JvmStatic
        fun from(messageActionsHistory: MessageActionsHistory): MessageActionsHistoryDTO {
            return MessageActionsHistoryDTO(
                id = messageActionsHistory.id,
                messageId = messageActionsHistory.message.id,
                status = messageActionsHistory.status,
                timestamp = messageActionsHistory.timestamp,
                comment = messageActionsHistory.comment
            )
        }
    }
}