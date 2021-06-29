package no.nav.samordning.hendelser.database

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {

    @Value("\${DESIRED_YTELSESTYPER}")
    private lateinit var ytelsesTyper: String

    val ytelsesFilter = "(SELECT(SELECT H2.HENDELSE_DATA #>> '{}' FROM HENDELSER H2 WHERE H2.ID = H.ID)::json->>'ytelsesType') IN ($ytelsesTyper)"
}