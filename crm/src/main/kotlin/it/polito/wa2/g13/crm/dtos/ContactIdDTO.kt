package it.polito.wa2.g13.crm.dtos

import jakarta.validation.constraints.Min

class ContactIdDTO (
    @field:Min(0)
    val contactId : Long
)