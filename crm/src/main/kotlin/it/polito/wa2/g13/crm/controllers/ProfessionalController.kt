package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.services.ProfessionalService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/professionals")
@Validated
class ProfessionalController(
    val professionalService: ProfessionalService
) {
    @GetMapping("", "/")
    fun getProfessionals(
        @RequestParam("page") @Min(0) page: Int,
        @RequestParam("limit") @Min(0) @Max(10) limit: Int,
        professionalFilters: ProfessionalFilters,
    ): List<ProfessionalDTO> {
        TODO("not implemented yet!")
    }
}