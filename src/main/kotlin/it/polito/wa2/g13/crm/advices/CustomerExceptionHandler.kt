package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.CustomerException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomerExceptionHandler {
    @ExceptionHandler(CustomerException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCustomerExceptionNotFound(e: CustomerException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(CustomerException.ContactAlreadyTaken::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleCustomerExceptionContactAlreadyTaken(e: CustomerException.ContactAlreadyTaken) = ProblemDetail
        .forStatusAndDetail(HttpStatus.FORBIDDEN, e.message)
}