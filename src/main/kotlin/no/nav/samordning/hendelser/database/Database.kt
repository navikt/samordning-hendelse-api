package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.hendelse.Hendelse
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import javax.json.bind.JsonbBuilder

@Repository
class Database {
    companion object {
        private const val LATEST_SNR_SQL = "SELECT MAX(ID) FROM HENDELSER WHERE TPNR = ?"
        private const val PAGE_COUNT_SQL = "SELECT COUNT(HENDELSE_DATA) FROM HENDELSER WHERE TPNR = ? AND ID >= ?"
        private const val HENDELSER_SQL = "SELECT HENDELSE_DATA #>> '{}' FROM HENDELSER WHERE ID >= ? AND TPNR = ? ORDER BY ID OFFSET ? LIMIT ?"
    }

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    val totalHendelser: String?
        get() = jdbcTemplate.queryForObject<String>("SELECT COUNT(*) FROM HENDELSER", String::class.java)

    fun fetchHendelser(tpnr: String, offset: Int, side: Int, antall: Int) =
            jdbcTemplate.queryForList<PGobject>(HENDELSER_SQL, PGobject::class.java, offset, tpnr, side * antall, antall)
                    .map { JsonbBuilder.create().fromJson<Hendelse>(it.value, Hendelse::class.java) }

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) =
            try {
                jdbcTemplate.queryForObject<String>(PAGE_COUNT_SQL, arrayOf(tpnr, sekvensnummer), String::class.java)
                        .toIntOrNull()
                        ?.let { (it + antall - 1) / antall }
                        ?: 0
            } catch (_: Exception) {
                0
            }

    fun latestSekvensnummer(tpnr: String): Int =
            try {
                jdbcTemplate.queryForObject<String>(LATEST_SNR_SQL, arrayOf<Any>(tpnr), String::class.java)
                        .toIntOrNull() ?: 1
            } catch (_: Exception) {
                1
            }
}