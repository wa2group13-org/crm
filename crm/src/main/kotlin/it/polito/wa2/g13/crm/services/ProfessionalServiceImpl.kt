package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.data.professional.Professional
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.repositories.ProfessionalRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ProfessionalServiceImpl(
    val professionalRepository: ProfessionalRepository
) : ProfessionalService {

    override fun getProfessionals(
        page: Int,
        limit: Int,
        professionalFilters: ProfessionalFilters
    ): List<ProfessionalDTO> {
        return professionalRepository
            .findAll(
                Professional.Spec.withFilters(professionalFilters),
                PageRequest.of(page, limit)
            )
            .map { ProfessionalDTO.from(it) }
            .toList()
    }
}