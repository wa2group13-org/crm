package it.polito.wa2.g13.crm.kafka

import it.polito.wa2.g13.crm.dtos.CreateMessageDTO
import it.polito.wa2.g13.crm.services.MessageService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaMessageConsumer(
    private val messageService: MessageService,
) {
    @KafkaListener(id = "crm-messages", topics = ["topic-crm-messages"])
    fun getMessage(record: ConsumerRecord<String, CreateMessageDTO>) {
        val message = record.value()!!
        println(message)
        messageService.createMessage(message)
    }
}