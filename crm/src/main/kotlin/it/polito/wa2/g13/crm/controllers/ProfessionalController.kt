package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.data.professional.EmploymentState
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.CreateSkillDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalFilters
import it.polito.wa2.g13.crm.exceptions.ProfessionalException
import it.polito.wa2.g13.crm.services.ProfessionalService
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import kotlin.Throws

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

    @DeleteMapping("/{id}")
    @Throws(ProfessionalException::class)
    fun deleteProfessional(@PathVariable("id") id: Long) {
        professionalService.deleteProfessional(id)
    }

    @PutMapping("/{id}/notes")
    @Throws(ProfessionalException::class)
    fun updateProfessionalNotes(
        @PathVariable("id") id: Long,
        @RequestBody @Valid @Size(max = 255) notes: String
    ) {
        return professionalService.updateProfessionalNotes(id, notes)
    }

    @PutMapping("/{id}/employmentState")
    @Throws(ProfessionalException::class)
    fun updateProfessionalEmploymentState(
        @PathVariable("id") id: Long,
        @RequestBody employmentState: EmploymentState,
    ) {
        return professionalService.updateProfessionalEmploymentState(id, employmentState)
    }

    @PutMapping("/{id}/dailyRate")
    fun updateProfessionalDailyRate(
        @PathVariable("id") id: Long,
        @RequestBody @DecimalMin("0.0") dailyRate: Double,
    ) {
        return professionalService.updateProfessionalDailyRate(id, dailyRate)
    }

    @PutMapping("/{id}/skills")
    fun updateProfessionalSkills(
        @PathVariable("id") id: Long,
        @RequestBody @Valid skills: Set<CreateSkillDTO>,
    ) {
        return professionalService.updateProfessionalSkills(id, skills)
    }

    @PutMapping("/{id}/contact")
    fun updateProfessionalContact(
        @PathVariable("id") id: Long,
        @RequestBody contactId: Long,
    ) {
        return professionalService.updateProfessionalContact(id, contactId)
    }
}