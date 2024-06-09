package it.polito.wa2.g13.crm.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import it.polito.wa2.g13.crm.properties.ProjectConfigProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    private val projectConfigProperties: ProjectConfigProperties,
) {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Customer Relationship Management API Documentation")
                .version(projectConfigProperties.version)
        )
}