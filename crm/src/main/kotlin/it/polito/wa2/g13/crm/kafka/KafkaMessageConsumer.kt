package it.polito.wa2.g13.crm.kafka

import it.polito.wa2.g13.crm.dtos.CreateMessageDTO
import it.polito.wa2.g13.crm.services.MessageService
import jakarta.validation.Valid
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class KafkaMessageConsumer(
    private val messageService: MessageService,
) {
    @KafkaListener(id = "crm-messages", topics = ["topic-crm-messages"])
    fun getMessage(@Payload @Valid message: CreateMessageDTO) {
        messageService.createMessage(message)
    }
}