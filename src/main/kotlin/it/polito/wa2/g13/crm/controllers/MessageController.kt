package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.data.message.Status
import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.repositories.SortBy
import it.polito.wa2.g13.crm.services.MessageService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/messages")
@Validated
class MessageController(
    private val messageService: MessageService,
) {
    @GetMapping("", "/")
    @ResponseStatus(HttpStatus.OK)
    fun getMessages(
        @RequestParam @Valid @Min(0, message = "page must be greater than zero") page: Int?,
        @RequestParam @Valid @Min(0, message = "limit must be between 0 and 100") limit: Int?,
        @RequestParam sortBy: SortBy?,
        @RequestParam filterByState: Status?
    ): Page<MessageDTO> {
        return messageService.listMessages(
            page, limit, sortBy, filterByState
        )
    }

    @PostMapping("", "/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMessage(
        @RequestBody @Valid message: CreateMessageDTO
    ): MessageDTO {
        return messageService.createMessage(message)
    }

    @GetMapping("/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    fun getMessageById(
        @PathVariable @Valid
        @Min(0, message = "messageId must be positive") messageId: Long
    ): MessageDTO {
        return messageService.getMessage(messageId)
    }

    @PostMapping("/{messageId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun changeMessageStatus(
        @PathVariable @Valid @Min(0, message = "messageId must be positive") messageId: Long,
        @RequestBody @Valid changeMessageStatus: ChangeMessageStatusDTO
    ): MessageDTO {
        return messageService.changeMessageState(messageId, changeMessageStatus.status, changeMessageStatus.comment)
    }

    @GetMapping("/{messageId}/history")
    @ResponseStatus(HttpStatus.OK)
    fun getMessageHistory(
        @PathVariable @Valid @Min(0, message = "messageId must be positive") messageId: Long
    ): List<MessageActionsHistoryDTO> {
        return messageService.getMessageHistory(messageId)
    }

    @PutMapping("/{messageId}/priority")
    @ResponseStatus(HttpStatus.OK)
    fun changeMessagePriority(
        @PathVariable @Valid @Min(0, message = "messageId must be positive") messageId: Long,
        @RequestBody @Valid changeMessagePriorityDTO: ChangeMessagePriorityDTO
    ): MessageDTO {
        return messageService.changeMessagePriority(messageId, changeMessagePriorityDTO.priority)
    }

    @GetMapping("/mailId/{mailId}")
    fun getMessageByMailId(
        @PathVariable mailId: String,
    ): MessageDTO {
        return messageService.getMessageByMailId(mailId)
    }

    @GetMapping("/contact/{contactId}")
    fun getMessagesByContactId(@PathVariable contactId: Long, @PageableDefault pageable: Pageable): Page<MessageDTO> {
        return messageService.getMessageByContactId(contactId, pageable)
    }
}

