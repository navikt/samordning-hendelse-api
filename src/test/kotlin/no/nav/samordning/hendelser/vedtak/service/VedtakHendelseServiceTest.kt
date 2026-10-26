package no.nav.samordning.hendelser.vedtak.service

import no.nav.samordning.hendelser.TestData
import no.nav.samordning.hendelser.common.feed.FeedBuilder
import no.nav.samordning.hendelser.config.IntegrationTest
import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import no.nav.samordning.hendelser.vedtak.repository.VedtakHendelseDO
import no.nav.samordning.hendelser.vedtak.repository.VedtakHendelseRepositoryDO
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
class VedtakHendelseServiceTest {

    @Autowired
    private lateinit var vedtakHendelseService: VedtakHendelseService

    @Autowired
    private lateinit var hendelseRepository: VedtakHendelseRepositoryDO

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @Transactional
    fun hendelse_store_and_fetch() {
        val samHendelse = SamHendelse("7000", "OMS", "01016700000", "8", "1122", "2023-12-01",null)
        val hendelseContainer = VedtakHendelseDO(samHendelse)
        val response = hendelseRepository.saveAndFlush(hendelseContainer)

        assertNotNull(hendelseContainer)
        assertEquals("7000", response.tpnr)

    }

    @Test
    fun expected_hendelse_is_fetched() {
        val expectedHendelse = TestData.h1000
        val hendelse = vedtakHendelseService.fetchSeqAndHendelser("1000", null, 0, 1).first()

        assertThat(hendelse, samePropertyValuesAs(expectedHendelse))
    }

    @Test
    fun multiple_expected_hendelser_are_fetched() {
        val expectedHendelser = listOf(TestData.h4000GP, TestData.h4000IP, TestData.h4000PT)
        val hendelser = vedtakHendelseService.fetchSeqAndHendelser("4000", 0, 0, 3)

        hendelser.forEachIndexed { i, hendelse ->
            assertThat(expectedHendelser[i], samePropertyValuesAs(hendelse))
        }
    }

    @Test
    fun hendelser_split_with_pagination() {
        val expectedPageOne = listOf(TestData.h4000GP, TestData.h4000IP)
        val expectedPageTwo = TestData.h4000PT

        val pageOne = vedtakHendelseService.fetchSeqAndHendelser("4000", null, 0, 2)
        val pageTwo = vedtakHendelseService.fetchSeqAndHendelser("4000", null, 1, 2)

        pageOne.forEachIndexed { i, hendelse ->
            assertThat(hendelse, samePropertyValuesAs(expectedPageOne[i]))
        }
        assertThat(pageTwo.first(), samePropertyValuesAs(expectedPageTwo))
    }

    @Test
    fun skip_hendelser_with_offset() {
        val expectedHendelse = TestData.h4000PT
        val hendelser = vedtakHendelseService.fetchSeqAndHendelser("4000", 3, 0, 3)

        assertEquals(1, hendelser.numberOfElements)
        assertThat(hendelser.first(), samePropertyValuesAs(expectedHendelse))
    }

    @Test
    @Transactional
    fun pagination_follows_sequence_numbers_not_storage_order() {
        jdbcTemplate.update(
            """
            INSERT INTO VEDTAK_HENDELSE(TPNR, IDENTIFIKATOR, YTELSES_TYPE, VEDTAK_ID, FOM)
            SELECT '3010', '01016600000', 'AAP', n::text, DATE '2026-07-01'
            FROM generate_series(1, 918) AS n
            ORDER BY n
            """.trimIndent()
        )
        jdbcTemplate.update(
            """
            UPDATE VEDTAK_HENDELSE
            SET SEKVENSNUMMER = CASE SEKVENSNUMMER
                WHEN 916 THEN 917
                WHEN 917 THEN 918
                WHEN 918 THEN 916
            END
            WHERE TPNR = '3010' AND SEKVENSNUMMER >= 916
            """.trimIndent()
        )

        val all = vedtakHendelseService.fetchSeqAndHendelser("3010", 1, 0, 1000)
        assertEquals((1L..918L).toList(), all.content.map { it.sekvensnummer })

        val lastTwo = vedtakHendelseService.fetchSeqAndHendelser("3010", 917, 0, 100)
        assertEquals(listOf(917L, 918L), lastTwo.content.map { it.sekvensnummer })
        assertEquals(918L, lastTwo.totalElements)

        val last = vedtakHendelseService.fetchSeqAndHendelser("3010", 918, 0, 100)
        assertEquals(listOf(918L), last.content.map { it.sekvensnummer })
        val feed = FeedBuilder("http://localhost")
            .forPath("/hendelser/vedtak")
            .withTpnr("3010")
            .withSekvensnummer(918)
            .build(last)
        assertEquals(918L, feed.sisteSekvensnummer)
        assertEquals(918L, feed.sisteLesteSekvensnummer)
        assertNull(feed.nextUrl)

        val firstPage = vedtakHendelseService.fetchSeqAndHendelser("3010", 917, 0, 1)
        val secondPage = vedtakHendelseService.fetchSeqAndHendelser("3010", 917, 1, 1)
        assertEquals(listOf(917L), firstPage.content.map { it.sekvensnummer })
        assertEquals(listOf(918L), secondPage.content.map { it.sekvensnummer })
    }
}
