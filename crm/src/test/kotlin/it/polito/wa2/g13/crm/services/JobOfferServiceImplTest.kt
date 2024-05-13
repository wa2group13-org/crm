package it.polito.wa2.g13.crm.services

import it.polito.wa2.g13.crm.IntegrationTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class JobOfferServiceImplTest : IntegrationTest() {
    companion object {
        private val logger = LoggerFactory.getLogger(JobOfferServiceImplTest::class.java)
    }

    @Autowired
    lateinit var jobOfferService: JobOfferService

    @Test
    fun `should do nothing`() {
        assert(true)
    }
}