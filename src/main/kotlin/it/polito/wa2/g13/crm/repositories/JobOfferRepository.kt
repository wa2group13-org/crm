package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.joboffer.JobOffer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface JobOfferRepository : JpaRepository<JobOffer, Long>, JpaSpecificationExecutor<JobOffer> {

}