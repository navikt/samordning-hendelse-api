package no.nav.samordning.hendelser

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class TestDataHelper(
    database: JdbcTemplate,
    private val mapper: ObjectMapper
) {

    private val hendelser: List<Hendelse> =
        database.queryForList("SELECT HENDELSE_DATA FROM HENDELSER", String::class.java)
            .map { mapper.readValue(it, Hendelse::class.java) }

    fun hendelse(identifikator: String) =
        hendelser.firstOrNull { it.identifikator == identifikator }

    fun hendelser(vararg identifikator: String) = hendelser
        .filter { it.identifikator in identifikator }
}