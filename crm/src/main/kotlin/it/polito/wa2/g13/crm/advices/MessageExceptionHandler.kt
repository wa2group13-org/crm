package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.MessageException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MessageExceptionHandler {
    @ExceptionHandler(MessageException.NotFound::class)
    fun handleMessageExceptionNotFound(e: MessageException.NotFound)
            : ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
    }

    @ExceptionHandler(MessageException.ForbiddenTransition::class)
    fun handleMessageExceptionForbiddenTransition(e: MessageException.ForbiddenTransition)
            : ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
    }
}