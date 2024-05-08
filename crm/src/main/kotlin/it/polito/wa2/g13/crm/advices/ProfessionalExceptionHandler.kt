package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ProfessionalExceptionHandler {
    @ExceptionHandler(ProfessionalException::class)
    fun handleProfessionalException(e: ProfessionalException): ProblemDetail {
        return when (e) {
            is ProfessionalException.NotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
        }
    }
}