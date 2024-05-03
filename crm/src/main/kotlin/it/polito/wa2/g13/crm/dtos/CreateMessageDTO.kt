package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.message.Priority
import jakarta.validation.constraints.NotBlank

data class CreateMessageDTO(
    @field:NotBlank(message = "sender cannot be null")
    val sender: String,
    @field:NotBlank(message = "channel cannot be null")
    val channel: String,
    val priority: Priority,
    val subject: String?,
    val body: String?,
)