package it.polito.wa2.g13.crm.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * These properties are used to configure the topics to receive
 * information on the kafka replicas.
 */
@ConfigurationProperties("kafka-config")
data class KafkaConfigProperties(
    /**
     * Topic of the mails in Kafka
     */
    @param:DefaultValue("mail.json")
    var mailTopic: String = "mail.json"
)