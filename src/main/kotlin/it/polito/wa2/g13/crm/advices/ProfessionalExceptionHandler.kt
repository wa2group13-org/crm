package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ProfessionalExceptionHandler {
    @ExceptionHandler(ProfessionalException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProfessionalExceptionNotFound(e: ProfessionalException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)


    @ExceptionHandler(ProfessionalException.InvalidContactState::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleProfessionalException(e: ProfessionalException.InvalidContactState) = ProblemDetail
        .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
}