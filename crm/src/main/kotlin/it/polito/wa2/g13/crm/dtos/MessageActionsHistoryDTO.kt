package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.message.MessageActionsHistory
import it.polito.wa2.g13.crm.data.message.Status
import java.time.OffsetDateTime

data class MessageActionsHistoryDTO(
    var messageId: Long,
    var status: Status,
    var timestamp: OffsetDateTime,
    var comment: String?,
) {
    companion object {
        @JvmStatic
        fun from(messageActionsHistory: MessageActionsHistory): MessageActionsHistoryDTO {
            return MessageActionsHistoryDTO(
                messageActionsHistory.message.id,
                messageActionsHistory.status,
                messageActionsHistory.timestamp,
                messageActionsHistory.comment
            )
        }
    }
}