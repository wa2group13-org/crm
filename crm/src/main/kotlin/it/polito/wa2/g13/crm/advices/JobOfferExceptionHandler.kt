package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.JobOfferException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class JobOfferExceptionHandler {
    @ExceptionHandler(JobOfferException::class)
    fun handleJobOfferExceptions(e: JobOfferException)
            : ProblemDetail {
        return when (e) {
            is JobOfferException.NotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
            is JobOfferException.ForbiddenTargetStatus -> ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, e.message
            )

            is JobOfferException.MissingProfessional -> ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, e.message
            )

            is JobOfferException.NoteNotFound -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
            is JobOfferException.TransitionStateError -> ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.message
            )
        }

    }
}