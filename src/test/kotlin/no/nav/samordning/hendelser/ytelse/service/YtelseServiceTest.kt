package no.nav.samordning.hendelser.ytelse.service

import no.nav.samordning.hendelser.common.feed.FeedBuilder
import no.nav.samordning.hendelser.config.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
class YtelseServiceTest {

    @Autowired
    private lateinit var ytelseService: YtelseService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @Transactional
    fun pagination_follows_sequence_numbers_not_storage_order() {
        jdbcTemplate.update(
            """
            INSERT INTO YTELSE_HENDELSER(
                TPNR, MOTTAKER, IDENTIFIKATOR, HENDELSE_TYPE, YTELSE_TYPE, DATO_BRUK_FOM
            )
            SELECT '3200', '7000', '01016600000', 'OPPRETT', 'ALDER', TIMESTAMP '2026-07-01 12:00:00'
            FROM generate_series(1, 3) AS n
            ORDER BY n
            """.trimIndent()
        )
        jdbcTemplate.update(
            """
            UPDATE YTELSE_HENDELSER
            SET SEKVENSNUMMER = CASE SEKVENSNUMMER
                WHEN 1 THEN 2
                WHEN 2 THEN 3
                WHEN 3 THEN 1
            END
            WHERE MOTTAKER = '7000'
            """.trimIndent()
        )

        val all = ytelseService.fetchSeqAndYtelseHendelser("7000", null, 0, 100)
        assertEquals(listOf(1L, 2L, 3L), all.content.map { it.sekvensnummer })
        assertEquals(3L, all.totalElements)
        assertEquals(listOf("3200", "3200", "3200"), all.content.map { it.tpnr })

        val lastTwo = ytelseService.fetchSeqAndYtelseHendelser("7000", 2, 0, 100)
        assertEquals(listOf(2L, 3L), lastTwo.content.map { it.sekvensnummer })

        val firstPage = ytelseService.fetchSeqAndYtelseHendelser("7000", 2, 0, 1)
        val secondPage = ytelseService.fetchSeqAndYtelseHendelser("7000", 2, 1, 1)
        assertEquals(listOf(2L), firstPage.content.map { it.sekvensnummer })
        assertEquals(listOf(3L), secondPage.content.map { it.sekvensnummer })

        val last = ytelseService.fetchSeqAndYtelseHendelser("7000", 3, 0, 100)
        assertEquals(listOf(3L), last.content.map { it.sekvensnummer })
        val feed = FeedBuilder("http://localhost")
            .forPath("/hendelser/ytelser")
            .withTpnr("7000")
            .withSekvensnummer(3)
            .build(last)
        assertEquals(3L, feed.sisteSekvensnummer)
        assertEquals(3L, feed.sisteLesteSekvensnummer)
        assertNull(feed.nextUrl)
    }
}
