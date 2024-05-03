package it.polito.wa2.g13.crm.dtos

import it.polito.wa2.g13.crm.data.message.Status

data class ChangeMessageStatusDTO(
    val status: Status,
    val comment: String?
)