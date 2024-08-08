package it.polito.wa2.g13.crm.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CustomerNoteDTO(
    @Size(max = 255) @NotBlank
    val note: String?
)