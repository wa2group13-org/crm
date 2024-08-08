package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.ContactException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ContactExceptionHandler {
    @ExceptionHandler(ContactException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleContactExceptionNotFound(e: ContactException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(ContactException.Duplicate::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleContactExceptionDuplicate(e: ContactException.Duplicate) = ProblemDetail
        .forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}