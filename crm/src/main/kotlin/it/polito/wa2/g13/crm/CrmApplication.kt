package it.polito.wa2.g13.crm

import it.polito.wa2.g13.crm.properties.KafkaConfigProperties
import it.polito.wa2.g13.crm.properties.ProjectConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	KafkaConfigProperties::class,
	ProjectConfigProperties::class,
)
class CrmApplication

fun main(args: Array<String>) {
	runApplication<CrmApplication>(*args)
}
