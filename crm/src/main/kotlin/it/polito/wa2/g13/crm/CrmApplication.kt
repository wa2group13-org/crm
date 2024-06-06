package it.polito.wa2.g13.crm

import it.polito.wa2.g13.crm.properties.KafkaConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	KafkaConfigProperties::class,
)
class CrmApplication

fun main(args: Array<String>) {
	runApplication<CrmApplication>(*args)
}
