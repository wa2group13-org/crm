package it.polito.wa2.g13.crm.repositories

import it.polito.wa2.g13.crm.data.professional.Professional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProfessionalRepository: JpaRepository<Professional, Long>, JpaSpecificationExecutor<Professional>