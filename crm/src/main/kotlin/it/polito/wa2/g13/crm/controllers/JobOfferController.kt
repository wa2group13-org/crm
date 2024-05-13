package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.*
import it.polito.wa2.g13.crm.services.JobOfferService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/joboffers")
@Validated
class JobOfferController(
    private val jobOfferService: JobOfferService,
) {

    @GetMapping("/{jobOfferId}")
    fun getJobOfferById(@PathVariable @Valid @Min(0) jobOfferId: Long): JobOfferDTO {
        return jobOfferService.getJobOfferById(jobOfferId)
    }

    @GetMapping("", "/")
    fun getJobOffers(
        @Valid getJobOffers: GetJobOffers
    ): Page<JobOfferDTO> {
        return jobOfferService.getJobOffersByParams(
            getJobOffers.filters,
            getJobOffers.page,
            getJobOffers.limit
        )
    }

    @PostMapping("")
    fun createJobOffer(@RequestBody createJobOfferDTO: CreateJobOfferDTO): JobOfferDTO {
        return jobOfferService.createJobOffer(createJobOfferDTO)
    }

    @GetMapping("/{jobOfferId}/value")
    fun getJobOfferValue(@PathVariable @Valid @Min(0) jobOfferId: Long): Double {
        return jobOfferService.getJobOfferValue(jobOfferId)
    }

    @PostMapping("/{jobOfferId}")
    fun changeJobOfferStatus(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @RequestBody @Valid updateJobOfferStatusDTO: UpdateJobOfferStatusDTO
    ): JobOfferDTO {
        return jobOfferService.updateJobOfferStatus(jobOfferId, updateJobOfferStatusDTO)
    }

    @PutMapping("/{jobOfferId}/details")
    fun changeJobOfferDetails(
        @PathVariable @Valid @Min(0) jobOfferId: Long,
        @RequestBody @Valid updateJobOfferDetailsDTO: UpdateJobOfferDetailsDTO
    ): JobOfferDTO {
        return jobOfferService.updateJobOfferDetails(jobOfferId, updateJobOfferDetailsDTO)
    }


    @DeleteMapping("/{jobOfferId}")
    fun deleteJobOffer(@PathVariable @Valid @Min(0) jobOfferId: Long) {
        return jobOfferService.deleteJobOffer(jobOfferId)
    }

    @GetMapping("/{jobOfferId}/notes/{noteId}")
    fun getNoteById(@PathVariable jobOfferId: Long, @PathVariable noteId: Long): JobOfferHistoryDTO {
        return jobOfferService.getNoteById(jobOfferId, noteId)
    }

    @GetMapping("/{jobOfferId}/notes")
    fun getNotesByJobOfferId(@PathVariable jobOfferId: Long): List<JobOfferHistoryDTO> {
        return jobOfferService.getNotesByJobOfferId(jobOfferId)
    }

    @PostMapping("/{jobOfferId}/notes")
    fun addNoteByJobOfferId(
        @PathVariable jobOfferId: Long,
        @RequestBody createJobOfferHistoryDTO: CreateJobOfferHistoryDTO
    ): JobOfferHistoryDTO {
        return jobOfferService.addNoteByJobOfferId(jobOfferId, createJobOfferHistoryDTO)
    }

    @PutMapping("/{jobOfferId}/notes/{noteId}")
    fun updateNoteById(
        @PathVariable jobOfferId: Long,
        @PathVariable noteId: Long,
        @RequestBody note: String?
    ): JobOfferHistoryDTO {
        return jobOfferService.updateNoteById(jobOfferId, noteId, note)
    }
}