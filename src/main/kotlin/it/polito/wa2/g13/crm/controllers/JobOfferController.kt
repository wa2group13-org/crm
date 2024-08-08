package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.services.JobOfferService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/joboffers")
@Validated
class JobOfferController(
    private val jobOfferService: JobOfferService,
) {

    @GetMapping("/{jobOfferId}")
    @ResponseStatus(HttpStatus.OK)
    fun getJobOfferById(@PathVariable @Valid @Min(0) jobOfferId: Long): JobOfferDTO {
        return jobOfferService.getJobOfferById(jobOfferId)
    }

    @GetMapping("", "/")
    @ResponseStatus(HttpStatus.OK)
    fun getJobOffers(
        @Valid getJobOffers: GetJobOffers
    ): Page<JobOfferDTO> {
        return jobOfferService.getJobOffersByParams(
            getJobOffers.filters,
            getJobOffers.page,
            getJobOffers.limit
        )
    }

    @PostMapping("", "/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createJobOffer(@RequestBody createJobOfferDTO: CreateJobOfferDTO): JobOfferDTO {
        return jobOfferService.createJobOffer(createJobOfferDTO)
    }

    @GetMapping("/{jobOfferId}/value")
    @ResponseStatus(HttpStatus.OK)
    fun getJobOfferValue(@PathVariable @Valid @Min(0) jobOfferId: Long): Double {
        return jobOfferService.getJobOfferValue(jobOfferId)
    }

    @PostMapping("/{jobOfferId}")
    @ResponseStatus(HttpStatus.OK)
    fun changeJobOfferStatus(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @RequestBody @Valid updateJobOfferStatusDTO: UpdateJobOfferStatusDTO
    ): JobOfferDTO {
        return jobOfferService.updateJobOfferStatus(jobOfferId, updateJobOfferStatusDTO)
    }

    @PutMapping("/{jobOfferId}/details")
    @ResponseStatus(HttpStatus.OK)
    fun changeJobOfferDetails(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @RequestBody @Valid updateJobOfferDetailsDTO: UpdateJobOfferDetailsDTO
    ): JobOfferDTO {
        return jobOfferService.updateJobOfferDetails(jobOfferId, updateJobOfferDetailsDTO)
    }

    @DeleteMapping("/{jobOfferId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteJobOffer(@PathVariable @Valid @Min(0) jobOfferId: Long) {
        return jobOfferService.deleteJobOffer(jobOfferId)
    }

    @GetMapping("/{jobOfferId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    fun getNoteById(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @PathVariable @Valid @Min(0) noteId: Long
    ): JobOfferHistoryDTO {
        return jobOfferService.getNoteById(jobOfferId, noteId)
    }

    @GetMapping("/{jobOfferId}/notes")
    @ResponseStatus(HttpStatus.OK)
    fun getNotesByJobOfferId(@PathVariable @Valid @Min(0) jobOfferId: Long): List<JobOfferHistoryDTO> {
        return jobOfferService.getNotesByJobOfferId(jobOfferId)
    }

    @PostMapping("/{jobOfferId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addNoteByJobOfferId(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @RequestBody createJobOfferHistoryNoteDTO: CreateJobOfferHistoryNoteDTO
    ): JobOfferHistoryDTO {
        return jobOfferService.addNoteByJobOfferId(jobOfferId, createJobOfferHistoryNoteDTO)
    }

    @PutMapping("/{jobOfferId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateNoteById(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @PathVariable @Valid @Min(0) noteId: Long,
        @RequestBody note: CreateJobOfferHistoryNoteDTO
    ): JobOfferHistoryDTO {
        return jobOfferService.updateNoteById(jobOfferId, noteId, note)
    }
}