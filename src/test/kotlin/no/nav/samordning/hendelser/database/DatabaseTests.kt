package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.TestDataHelper
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DatabaseTests {

    @Autowired
    private lateinit var db: Database

    @Autowired
    private lateinit var testData: TestDataHelper

    @Test
    fun expected_hendelse_is_fetched() {
        val expectedHendelse = testData.hendelse("01016600000")
        val hendelse = db.fetchHendelser("1000", 0, 0, 1)[0]

        assertThat<Hendelse>(expectedHendelse, samePropertyValuesAs<Hendelse>(hendelse))
    }

    @Test
    fun expected_hendelse_from_multiple_tpnr_is_fetched() {
        val expectedHendelse = testData.hendelse("01016700000")
        val hendelse1 = db.fetchHendelser("2000", 0, 0, 2)[0]
        val hendelse2 = db.fetchHendelser("3000", 0, 0, 2)[0]

        assertThat<Hendelse>(expectedHendelse, samePropertyValuesAs<Hendelse>(hendelse1))
        assertThat<Hendelse>(expectedHendelse, samePropertyValuesAs<Hendelse>(hendelse2))
    }

    @Test
    fun multiple_expected_hendelser_are_fetched() {
        val expectedHendelser = testData.hendelser("01016800000", "01016900000", "01017000000")
        val hendelser = db.fetchHendelser("4000", 0, 0, 3)

        hendelser.forEachIndexed { i, hendelse ->
            assertThat<Hendelse>(hendelse, samePropertyValuesAs<Hendelse>(expectedHendelser[i]))
        }
    }

    @Test
    fun unknown_hendelse_returns_empty_list() {
        assertEquals(emptyList<Any>(), db.fetchHendelser("1234", 1, 0, 9999))
    }

    @Test
    fun page_counting() {
        assertEquals(1, db.getNumberOfPages("2000", 1, 1))
        assertEquals(3, db.getNumberOfPages("4000", 1, 1))
        assertEquals(2, db.getNumberOfPages("4000", 1, 2))
        assertEquals(1, db.getNumberOfPages("4000", 5, 2))
        assertEquals(0, db.getNumberOfPages("4000", 7, 1))
    }

    @Test
    fun hendelser_split_with_pagination() {
        val expectedPageOne = testData.hendelser("01016800000", "01016900000")
        val expectedPageTwo = testData.hendelser("01017000000")

        val pageOne = db.fetchHendelser("4000", 0, 0, 2)
        val pageTwo = db.fetchHendelser("4000", 0, 1, 2)

        pageOne.forEachIndexed { i, hendelse ->
            assertThat<Hendelse>(hendelse, samePropertyValuesAs<Hendelse>(expectedPageOne[i]))
        }
        assertThat<Hendelse>(pageTwo[0], samePropertyValuesAs<Hendelse>(expectedPageTwo[0]))
    }

    @Test
    fun skip_hendelser_with_offset() {
        val expectedHendelse = testData.hendelse("01017000000")
        val hendelser = db.fetchHendelser("4000", 6, 0, 3)

        assertEquals(1, hendelser.size)
        assertThat<Hendelse>(hendelser[0], samePropertyValuesAs<Hendelse>(expectedHendelse!!))
    }

    @Test
    fun latest_sekvensnummer_for_tpnr() {
        val expectedSekvensnummer = 6
        val latestSekvensnummer = db.latestSekvensnummer("4000")

        assertEquals(expectedSekvensnummer, latestSekvensnummer)
    }

    @Test
    fun latest_sekvensnummer_for_tpnr_when_no_hendelser() {
        val expectedSekvensnummer = 1
        val latestSekvensnummer = db.latestSekvensnummer("1234")

        assertEquals(expectedSekvensnummer, latestSekvensnummer)
    }

    @Test
    fun filter_ytelsestyper_from_databaseconfig() {
        val expectedHendelser = emptyList<Hendelse>()

        assertEquals(expectedHendelser, db.fetchHendelser("5000", 0, 0, 1))
    }
}