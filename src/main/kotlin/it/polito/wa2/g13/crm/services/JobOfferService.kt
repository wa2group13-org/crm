package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.dtos.*
import org.springframework.data.domain.Page

interface JobOfferService {
    fun getJobOfferById(id: Long): JobOfferDTO
    fun getJobOffersByParams(filters: JobOfferFilters?, page: Int, limit: Int): Page<JobOfferDTO>
    fun createJobOffer(createJobOfferDTO: CreateJobOfferDTO): JobOfferDTO
    fun getJobOfferValue(id: Long): Double
    fun updateJobOfferDetails(id: Long, updateJobOfferDetailsDTO: UpdateJobOfferDetailsDTO): JobOfferDTO
    fun updateJobOfferStatus(id: Long, updateJobOfferStatusDTO: UpdateJobOfferStatusDTO): JobOfferDTO
    fun deleteJobOffer(id: Long)

    fun getNoteById(jobOfferId: Long, noteId: Long): JobOfferHistoryDTO
    fun getNotesByJobOfferId(id: Long): List<JobOfferHistoryDTO>
    fun addNoteByJobOfferId(id: Long, note: CreateJobOfferHistoryNoteDTO): JobOfferHistoryDTO
    fun updateNoteById(jobOfferId: Long, noteId: Long, note: CreateJobOfferHistoryNoteDTO): JobOfferHistoryDTO
}