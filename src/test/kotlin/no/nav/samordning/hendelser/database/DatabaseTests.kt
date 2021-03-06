package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.TestDataHelper
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class DatabaseTests {

    @Autowired
    private lateinit var db: Database

    @Autowired
    private lateinit var testData: TestDataHelper

    @Test
    fun expected_hendelse_is_fetched() {
        val expectedHendelse = testData.hendelse("01016600000")
        val hendelse = db.fetchSeqAndHendelser("1000", 0, 0, 1).values.first()

        assertThat<Hendelse>(expectedHendelse, samePropertyValuesAs<Hendelse>(hendelse))
    }

    @Test
    fun expected_hendelse_from_multiple_tpnr_is_fetched() {
        val expectedHendelse = testData.hendelse("01016700000")
        val hendelse1 = db.fetchSeqAndHendelser("2000", 0, 0, 2).values.first()
        val hendelse2 = db.fetchSeqAndHendelser("3000", 0, 0, 2).values.first()

        assertThat(expectedHendelse, samePropertyValuesAs(hendelse1))
        assertThat<Hendelse>(expectedHendelse, samePropertyValuesAs<Hendelse>(hendelse2))
    }

    @Test
    fun multiple_expected_hendelser_are_fetched() {
        val expectedHendelser = testData.hendelser("01016800000", "01016900000", "01017000000")
        val hendelser = db.fetchSeqAndHendelser("4000", 0, 0, 3)

        hendelser.values.forEachIndexed { i, hendelse ->
            assertThat<Hendelse>(hendelse, samePropertyValuesAs<Hendelse>(expectedHendelser[i]))
        }
    }

    @Test
    fun unknown_hendelse_returns_empty_list() {
        assertEquals(emptyMap<Long, Hendelse>(), db.fetchSeqAndHendelser("1234", 1, 0, 9999))
    }

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
        val expectedPageOne = testData.hendelser("01016800000", "01016900000")
        val expectedPageTwo = testData.hendelser("01017000000")

        val pageOne = db.fetchSeqAndHendelser("4000", 0, 0, 2)
        val pageTwo = db.fetchSeqAndHendelser("4000", 0, 1, 2)

        pageOne.values.forEachIndexed { i, hendelse ->
            assertThat<Hendelse>(hendelse, samePropertyValuesAs<Hendelse>(expectedPageOne[i]))
        }
        assertThat<Hendelse>(pageTwo.values.first(), samePropertyValuesAs<Hendelse>(expectedPageTwo.first()))
    }

    @Test
    fun skip_hendelser_with_offset() {
        val expectedHendelse = testData.hendelse("01017000000")
        val hendelser = db.fetchSeqAndHendelser("4000", 3, 0, 3)

        assertEquals(1, hendelser.size)
        assertThat<Hendelse>(hendelser.values.first(), samePropertyValuesAs<Hendelse>(expectedHendelse!!))
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