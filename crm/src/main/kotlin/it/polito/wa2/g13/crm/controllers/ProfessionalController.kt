package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.services.ProfessionalService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import kotlin.jvm.Throws

@RestController
@RequestMapping("/API/professionals")
@Validated
class ProfessionalController(
    val professionalService: ProfessionalService
) {
    @GetMapping("")
    fun getProfessionals(
        @RequestParam("page") @Min(0) page: Int,
        @RequestParam("limit") @Min(0) @Max(10) limit: Int,
        professionalFilters: ProfessionalFilters,
    ): List<ProfessionalDTO> {
        return professionalService.getProfessionals(page, limit, professionalFilters)
    }

    @PostMapping("")
    fun createProfessional(
        @RequestBody professional: CreateProfessionalDTO
    ): ResponseEntity<Unit> {
        val newId = professionalService.createProfessional(professional)

        return ResponseEntity.created(URI.create("/API/professionals/$newId")).build()
    }

    @GetMapping("/{id}")
    @Throws(ProfessionalException::class)
    fun getProfessional(@PathVariable("id") id: Long): ProfessionalDTO {
        return professionalService.getProfessional(id)
    }

    @PutMapping("/{id}")
    fun updateProfessional(
        @PathVariable("id") id: Long,
        @RequestBody @Valid professional: CreateProfessionalDTO
    ): ResponseEntity<Unit> {
        val newId = professionalService.updateProfessional(id, professional)

        return if (newId != null) {
            ResponseEntity.created(URI.create("/API/professionals/$newId")).build()
        } else {
            ResponseEntity.noContent().build()
        }
    }
}