package no.nav.samordning.hendelser.common.feed

import no.nav.samordning.hendelser.config.IntegrationTest
import no.nav.samordning.hendelser.manglendeRefusjonskrav.service.ManglendeRefusjonskravService
import no.nav.samordning.hendelser.person.service.PersonService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@IntegrationTest
class SequencePaginationTest {

    @Autowired
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var refusjonskravService: ManglendeRefusjonskravService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @ParameterizedTest
    @ValueSource(strings = ["PERSON_ENDRING", "MANGLENDE_REFUSJONSKRAV"])
    @Transactional
    fun pagination_follows_sequence_numbers_not_storage_order(table: String) {
        val insert = when (table) {
            "PERSON_ENDRING" -> """
                INSERT INTO PERSON_ENDRING(TPNR, FNR, MELDINGSKODE)
                SELECT tp, '01016600000', 'SIVILSTAND'
                FROM (VALUES ('7000'), ('7001')) AS t(tp), generate_series(1, 3) AS n
                ORDER BY tp, n
            """
            else -> """
                INSERT INTO MANGLENDE_REFUSJONSKRAV(TPNR, FNR, SAM_ID, SVARFRIST)
                SELECT tp, '01016600000', tp || n::text, DATE '2026-12-01'
                FROM (VALUES ('7000'), ('7001')) AS t(tp), generate_series(1, 3) AS n
                ORDER BY tp, n
            """
        }
        jdbcTemplate.update(insert.trimIndent())
        jdbcTemplate.update(
            """
            UPDATE $table
            SET SEKVENSNUMMER = CASE SEKVENSNUMMER
                WHEN 1 THEN 2
                WHEN 2 THEN 3
                WHEN 3 THEN 1
            END
            WHERE TPNR = '7000'
            """.trimIndent()
        )

        val path = if (table == "PERSON_ENDRING") "/hendelser/personer" else "/hendelser/manglendeRefusjonskrav"
        fun fetch(start: Long?, side: Int, antall: Int): Feed<out SequentialDTO> {
            val builder = FeedBuilder("http://localhost").forPath(path)
                .withTpnr("7000").withSekvensnummer(start)
            return if (table == "PERSON_ENDRING") {
                builder.build(personService.fetchSeqAndPersonEndringHendelser("7000", start, side, antall))
            } else {
                builder.build(refusjonskravService.fetchSeqAndManglendeRefusjonskravHendelser("7000", start, side, antall))
            }
        }

        val all = fetch(null, 0, 100)
        assertEquals(listOf(1L, 2L, 3L), all.hendelser.map { it.sekvensnummer })
        assertEquals(3L, all.sisteSekvensnummer)

        val lastTwo = fetch(2, 0, 100)
        assertEquals(listOf(2L, 3L), lastTwo.hendelser.map { it.sekvensnummer })
        assertNull(lastTwo.nextUrl)

        val firstPage = fetch(2, 0, 1)
        val secondPage = fetch(2, 1, 1)
        assertEquals(listOf(2L), firstPage.hendelser.map { it.sekvensnummer })
        assertEquals(URI("http://localhost$path?tpnr=7000&sekvensnummer=2&antall=1&side=1"), firstPage.nextUrl)
        assertEquals(listOf(3L), secondPage.hendelser.map { it.sekvensnummer })

        val last = fetch(3, 0, 100)
        assertEquals(listOf(3L), last.hendelser.map { it.sekvensnummer })
        assertEquals(3L, last.sisteSekvensnummer)
        assertEquals(3L, last.sisteLesteSekvensnummer)
        assertNull(last.nextUrl)
    }
}
