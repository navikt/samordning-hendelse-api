package no.nav.samordning.hendelser.database

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {

    @Value("\${DESIRED_YTELSESTYPER}")
    private lateinit var ytelsesTyper: String

    val ytelsesFilter: String
        get() = ytelsesTyper.split(',').joinToString(prefix = "HENDELSE_DATA ->> 'ytelsesType' IN ('", separator = "', '", postfix = "')")
}