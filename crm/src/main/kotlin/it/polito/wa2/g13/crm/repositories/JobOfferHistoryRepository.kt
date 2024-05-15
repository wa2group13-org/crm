package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.joboffer.JobOfferHistory
import org.springframework.data.jpa.repository.JpaRepository

interface JobOfferHistoryRepository : JpaRepository<JobOfferHistory, Long> {
}