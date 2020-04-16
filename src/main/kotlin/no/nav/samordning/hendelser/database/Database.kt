package no.nav.samordning.hendelser.database

import com.google.gson.Gson
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import javax.json.bind.JsonbBuilder

@Repository
class Database (databaseConfig: DatabaseConfig) {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val hendelseFilterSql = "ID >= ? AND TPNR = ?"
    private val totalHendelserSql = "SELECT COUNT(*) FROM HENDELSER"
    private val latestSnrSql = "SELECT MAX(ID) FROM HENDELSER WHERE TPNR = ?"
    private val pageCountSql = "SELECT COUNT(HENDELSE_DATA) FROM HENDELSER WHERE $hendelseFilterSql"
    private val hendelserSql = "SELECT HENDELSE_DATA #>> '{}' FROM HENDELSER WHERE $hendelseFilterSql AND ${databaseConfig.ytelsesFilter} ORDER BY ID OFFSET ? LIMIT ?"
    private val readSnrsSql = "SELECT ID FROM HENDELSER WHERE $hendelseFilterSql AND ${databaseConfig.ytelsesFilter} ORDER BY ID OFFSET ? LIMIT ?"

    private val seqAndHendelseSql = "SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = ? ORDER BY ID) AS SEQ, HENDELSE_DATA #>> '{}' AS DATA FROM HENDELSER WHERE TPNR = ? AND ${databaseConfig.ytelsesFilter} OFFSET ? LIMIT ?"

    val totalHendelser: String?
        get() = jdbcTemplate.queryForObject<String>(totalHendelserSql, String::class.java)

    fun fetchHendelser(tpnr: String, offset: Int, side: Int, antall: Int) =
            jdbcTemplate.queryForList<PGobject>(hendelserSql, PGobject::class.java, offset, tpnr, side * antall, antall)
                    .map { JsonbBuilder.create().fromJson<Hendelse>(it.value, Hendelse::class.java) }

    fun fetchLatestReadSekvensnummer(tpnr: String, offset: Int, side: Int, antall: Int) =
            jdbcTemplate.queryForList<String>(readSnrsSql, String::class.java, offset, tpnr, side * antall, antall)
                    .mapNotNull(String::toIntOrNull).max() ?: 1

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) =
            try {
                jdbcTemplate.queryForObject<String>(pageCountSql, String::class.java, sekvensnummer, tpnr)
                        .toIntOrNull()
                        ?.let { (it + antall - 1) / antall }
                        ?: 0
            } catch (_: Exception) {
                0
            }

    fun latestSekvensnummer(tpnr: String): Int =
            try {
                jdbcTemplate.queryForObject<String>(latestSnrSql, String::class.java, tpnr)
                        .toIntOrNull() ?: 1
            } catch (_: Exception) {
                1
            }

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Int, side: Int, antall: Int) =
            jdbcTemplate.queryForList(seqAndHendelseSql, tpnr, tpnr, sekvensnummer+(side*antall), antall)
                    .map {
                        it.getValue("seq") as Long to Gson().fromJson(it.getValue("data") as String, Hendelse::class.java)
                    }.toMap()
}