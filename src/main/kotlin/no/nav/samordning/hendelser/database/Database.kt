package no.nav.samordning.hendelser.database

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import kotlin.math.ceil

@Repository
class Database(
    databaseConfig: DatabaseConfig,
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: ObjectMapper
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(Database::class.java)
    }

    private val totalHendelserSql = "SELECT COUNT(*) FROM HENDELSER H"
    private val tpnrHendelserSql = "$totalHendelserSql WHERE H.TPNR = ? AND ${databaseConfig.ytelsesFilter}"
    private val seqAndHendelseSql =
        "SELECT ROW_NUMBER() OVER(PARTITION BY H.TPNR = ? ORDER BY H.ID) AS SEQ, H.HENDELSE_DATA #>> '{}' AS DATA FROM HENDELSER H WHERE H.TPNR = ? AND ${databaseConfig.ytelsesFilter} OFFSET ? LIMIT ?"

    val totalHendelser: String?
        get() = jdbcTemplate.queryForObject(totalHendelserSql, String::class.java)

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) =
        try {
            jdbcTemplate.queryForObject<Int>(tpnrHendelserSql, Int::class.java, tpnr)
                .minus(sekvensnummer - 1)
                .div(antall.toDouble())
                .let(::ceil).toInt()
        } catch (e: Exception) {
            LOG.warn(e.message)
            0
        }

    fun latestSekvensnummer(tpnr: String) =
        try {
            jdbcTemplate.queryForObject<Long>(tpnrHendelserSql, Long::class.java, tpnr)
        } catch (e: Exception) {
            LOG.warn(e.message)
            1L
        }!!

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Int, side: Int, antall: Int) =
        jdbcTemplate.queryForList(
            seqAndHendelseSql,
            tpnr,
            tpnr,
            sekvensnummer.coerceAtLeast(1) + (side * antall) - 1,
            antall
        )
            .associate {
                it.getValue("seq") as Long to mapper.readValue(it.getValue("data") as String, Hendelse::class.java)
            }.toMap()
}