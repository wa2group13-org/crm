package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.dtos.CreateProfessionalDTO
import it.polito.wa2.g13.crm.dtos.ProfessionalDTO
import it.polito.wa2.g13.crm.services.ProfessionalService
import it.polito.wa2.g13.crm.utils.randomProfessionals
import org.assertj.core.api.Assertions
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.RequestEntity
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = ["/scripts/clean_db.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfessionalControllerTest: IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(ProfessionalController::class.java)

        private val professionals = randomProfessionals(10, 5)
    }

    @Autowired
    private lateinit var professionalService: ProfessionalService

    @BeforeEach
    fun createDb() {
        professionals.forEach {
            professionalService.createProfessional(it)
        }
    }

    @Autowired
    private lateinit var restClient: TestRestTemplate

    @Test
    fun `get all professionals should match inserted ones`() {
        val limit = 10
        val page = 0

        val req = RequestEntity
            .get("/API/professionals?limit=$limit&page=$page")
            .build()

        val res = restClient.exchange<List<ProfessionalDTO>>(req)

        assertEquals(true, res.statusCode.is2xxSuccessful)
        Assertions.assertThat(res.body?.map { CreateProfessionalDTO.from(it) })
            .usingRecursiveComparison(
                RecursiveComparisonConfiguration
                    .builder()
                    .withIgnoreCollectionOrder(true)
                    .build()
            )
            .isEqualTo(professionals)
    }
}