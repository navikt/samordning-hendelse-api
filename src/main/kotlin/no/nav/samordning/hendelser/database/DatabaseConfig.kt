package no.nav.samordning.hendelser.database

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig(
    @Value("\${DESIRED_YTELSESTYPER}")
    val ytelsesTyper: Array<String>
)