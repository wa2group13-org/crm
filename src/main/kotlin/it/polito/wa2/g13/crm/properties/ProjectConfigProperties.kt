package it.polito.wa2.g13.crm.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.PropertySource

/**
 * Need to use [EnableConfigurationProperties] in order to scan properties in other
 * property files.
 *
 * **Important**: this class should **NOT** be set, it will be automatically
 * inserted during compile time by a gradle task!!
 */
@EnableConfigurationProperties
@ConfigurationProperties("project")
@PropertySource("classpath:project.properties")
data class ProjectConfigProperties(
    /**
     * This is the current version of the project.
     *
     * **Important**: this filed should **NOT** be set, it will be automatically
     * inserted during compile time by a gradle task!!
     */
    var version: String
)