package it.polito.wa2.g13.crm.controllers

import it.polito.wa2.g13.crm.IntegrationTest
import it.polito.wa2.g13.crm.services.JobOfferService
import it.polito.wa2.g13.crm.services.JobOfferServiceImplTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.jdbc.Sql

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = ["/scripts/clean_db.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JobOfferControllerTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(JobOfferServiceImplTest::class.java)
    }

    @BeforeEach
    fun setupDatabase(@Autowired jobOfferService: JobOfferService) {
        //create some job offers
    }

    @Autowired
    private lateinit var restClient: TestRestTemplate

    @Test
    fun `get jobOffers`() {

    }
}