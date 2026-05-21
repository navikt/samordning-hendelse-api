package no.nav.samordning.hendelser.vedtak.service
import no.nav.samordning.hendelser.config.IntegrationTest

import no.nav.samordning.hendelser.TestData
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseContainerDO
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepositoryDO
import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
class HendelseServiceTest {

    @Autowired
    private lateinit var hendelseService: HendelseService

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
        val expectedHendelse = TestData.h1000
        val hendelse = hendelseService.fetchSeqAndHendelser("1000", null, 0, 1).first()

        assertThat(hendelse, samePropertyValuesAs(expectedHendelse))
    }

    @Test
    fun multiple_expected_hendelser_are_fetched() {
        val expectedHendelser = listOf(TestData.h4000GP, TestData.h4000IP, TestData.h4000PT)
        val hendelser = hendelseService.fetchSeqAndHendelser("4000", 0, 0, 3)

        hendelser.forEachIndexed { i, hendelse ->
            assertThat(expectedHendelser[i], samePropertyValuesAs(hendelse))
        }
    }

    @Test
    fun hendelser_split_with_pagination() {
        val expectedPageOne = listOf(TestData.h4000GP, TestData.h4000IP)
        val expectedPageTwo = TestData.h4000PT

        val pageOne = hendelseService.fetchSeqAndHendelser("4000", null, 0, 2)
        val pageTwo = hendelseService.fetchSeqAndHendelser("4000", null, 1, 2)

        pageOne.forEachIndexed { i, hendelse ->
            assertThat(hendelse, samePropertyValuesAs(expectedPageOne[i]))
        }
        assertThat(pageTwo.first(), samePropertyValuesAs(expectedPageTwo))
    }

    @Test
    fun skip_hendelser_with_offset() {
        val expectedHendelse = TestData.h4000PT
        val hendelser = hendelseService.fetchSeqAndHendelser("4000", 3, 0, 3)

        assertEquals(1, hendelser.numberOfElements)
        assertThat(hendelser.first(), samePropertyValuesAs(expectedHendelse))
    }

    @Test
    fun filter_ytelsestyper_from_databaseconfig() {
        assertTrue(hendelseService.fetchSeqAndHendelser("5000", null, 0, 1).isEmpty())
    }
}
