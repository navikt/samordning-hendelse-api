package no.nav.samordning.hendelser.database

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.TestData
import no.nav.samordning.hendelser.hendelse.HendelseContainerDO
import no.nav.samordning.hendelser.hendelse.HendelseRepositoryDO
import no.nav.samordning.hendelser.kafka.SamHendelse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@Import(DatabaseConfig::class, HendelseService::class)
class HendelseServiceTests {

    @Autowired
    private lateinit var db: HendelseService

    @Autowired
    private lateinit var hendelseRepository: HendelseRepositoryDO

    @Test
    @Transactional
    fun hendelse_store_and_fetch() {
        val samHendelse = SamHendelse("7000", "OMS", "01016700000", "8", "1122", "2023-12-01",null)
        val hendelseContainer = HendelseContainerDO(samHendelse)
        val response = hendelseRepository.saveAndFlush(hendelseContainer)

        assertNotNull(hendelseContainer)
        assertEquals("7000", response.tpnr)

    }

    @Test
    fun expected_hendelse_is_fetched() {
        val expectedHendelse = TestData.h1000.hendelseData
        val hendelse = db.fetchSeqAndHendelser("1000", 0, 0, 1).values.first()

        assertThat(hendelse, samePropertyValuesAs(expectedHendelse))
    }

    @Test
    fun multiple_expected_hendelser_are_fetched() {
        val expectedHendelser = listOf(TestData.h4000GP.hendelseData, TestData.h4000IP.hendelseData, TestData.h4000PT.hendelseData)
        val hendelser = db.fetchSeqAndHendelser("4000", 0, 0, 3)

        hendelser.values.forEachIndexed { i, hendelse ->
            assertThat(expectedHendelser[i], samePropertyValuesAs(hendelse))
        }
    }

//    @Test
//    fun unknown_hendelse_returns_empty_list() {
//        assertEquals(emptyMap(), db.fetchSeqAndHendelser("1234", 1, 0, 9999))
//    }

    @Test
    fun page_counting() {
        assertEquals(1, db.getNumberOfPages("2000", 1, 1))
        assertEquals(3, db.getNumberOfPages("4000", 1, 1))
        assertEquals(2, db.getNumberOfPages("4000", 1, 2))
        assertEquals(1, db.getNumberOfPages("4000", 2, 2))
        assertEquals(0, db.getNumberOfPages("4000", 4, 1))
    }

    @Test
    fun hendelser_split_with_pagination() {
        val expectedPageOne = listOf(TestData.h4000GP.hendelseData, TestData.h4000IP.hendelseData)
        val expectedPageTwo = TestData.h4000PT.hendelseData

        val pageOne = db.fetchSeqAndHendelser("4000", 0, 0, 2)
        val pageTwo = db.fetchSeqAndHendelser("4000", 0, 1, 2)

        pageOne.values.forEachIndexed { i, hendelse ->
            assertThat(hendelse, samePropertyValuesAs(expectedPageOne[i]))
        }
        assertThat(pageTwo.values.first(), samePropertyValuesAs(expectedPageTwo))
    }

    @Test
    fun skip_hendelser_with_offset() {
        val expectedHendelse = TestData.h4000PT.hendelseData
        val hendelser = db.fetchSeqAndHendelser("4000", 3, 0, 3)

        assertEquals(1, hendelser.size)
        assertThat(hendelser.values.first(), samePropertyValuesAs(expectedHendelse))
    }

    @Test
    fun latest_sekvensnummer_for_tpnr() {
        val expectedSekvensnummer = 3L
        val latestSekvensnummer = db.latestSekvensnummer("4000")

        assertEquals(expectedSekvensnummer, latestSekvensnummer)
    }

    @Test
    fun latest_sekvensnummer_for_tpnr_when_no_hendelser() {
        val expectedSekvensnummer = 0L
        val latestSekvensnummer = db.latestSekvensnummer("1234")

        assertEquals(expectedSekvensnummer, latestSekvensnummer)
    }

    @Test
    fun filter_ytelsestyper_from_databaseconfig() {
        assertTrue(db.fetchSeqAndHendelser("5000", 0, 0, 1).isEmpty())
    }
}