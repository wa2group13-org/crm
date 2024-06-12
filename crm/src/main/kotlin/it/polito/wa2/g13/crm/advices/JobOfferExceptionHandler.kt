package it.polito.wa2.g13.crm.advices

import it.polito.wa2.g13.crm.exceptions.JobOfferException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class JobOfferExceptionHandler {
    @ExceptionHandler(JobOfferException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun jobOfferExceptionNotFound(e: JobOfferException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(JobOfferException.ForbiddenTargetStatus::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun jobOfferExceptionForbiddenTargetStatus(e: JobOfferException.ForbiddenTargetStatus) = ProblemDetail
        .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(JobOfferException.MissingProfessional::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun jobOfferExceptionMissingProfessional(e: JobOfferException.MissingProfessional) = ProblemDetail
        .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(JobOfferException.NoteNotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun jobOfferExceptionNoteNotFound(e: JobOfferException.NoteNotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(JobOfferException.TransitionStateError::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun jobOfferExceptionTransitionStateError(e: JobOfferException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(JobOfferException.IllegalProfessionalState::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun jobOfferExceptionIllegalProfessionalState(e: JobOfferException.NotFound) = ProblemDetail
        .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
}