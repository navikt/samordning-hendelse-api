package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.hendelse.Hendelse
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.json.bind.JsonbBuilder

@Configuration
class TestDataHelper(database: JdbcTemplate) {

    private val hendelser: List<Hendelse> = database.queryForList("SELECT HENDELSE_DATA FROM HENDELSER", PGobject::class.java)
            .map { JsonbBuilder.create().fromJson(it.value, Hendelse::class.java) }

    fun hendelse(identifikator: String) =
            hendelser.firstOrNull { it.identifikator == identifikator }

    fun hendelser(vararg identifikator: String) = hendelser
            .filter { it.identifikator in identifikator }
}