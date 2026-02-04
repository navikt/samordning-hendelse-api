package no.nav.samordning.hendelser.vedtak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper
import tools.jackson.datatype.jsr310.JavaTimeModule
import tools.jackson.module.kotlin.jacksonMapperBuilder

@Configuration
class DatabaseConfig(
    @Value("\${DESIRED_YTELSESTYPER}")
    val ytelsesTyper: Array<String>
) {

    @Bean
    fun objectMapper(): ObjectMapper = jacksonMapperBuilder().addModule(JavaTimeModule()).build()
}
