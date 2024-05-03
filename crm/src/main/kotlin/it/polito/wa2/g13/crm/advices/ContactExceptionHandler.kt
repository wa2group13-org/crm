package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.ContactException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ContactExceptionHandler {
    @ExceptionHandler(ContactException::class)
    fun handleContactException(e: ContactException): ProblemDetail {
        return when (e) {
            is ContactException.NotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
            is ContactException.Duplicate -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
        }
    }
}