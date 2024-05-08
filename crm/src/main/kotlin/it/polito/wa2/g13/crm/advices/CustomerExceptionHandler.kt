package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.CustomerException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomerExceptionHandler {
    @ExceptionHandler(CustomerException::class)
    fun handleCustomerException(e: CustomerException) : ProblemDetail{
        return when(e) {
            is CustomerException.NotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
            is CustomerException.ContactAlreadyTaken -> ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message)
        }
    }


}