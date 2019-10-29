package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@SpringBootTest
public class DatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private TestDataHelper testData;

    @Test
    public void expected_hendelse_is_fetched() {
        var expectedHendelse = testData.hendelse("01016600000");
        var hendelse = db.fetchHendelser("1000", 0, 0 ,1).get(0);

        assertThat(expectedHendelse, samePropertyValuesAs(hendelse));
    }

    @Test
    public void expected_hendelse_from_multiple_tpnr_is_fetched() {
        var expectedHendelse = testData.hendelse("01016700000");
        var hendelse1 = db.fetchHendelser("2000", 0, 0 ,2).get(0);
        var hendelse2 = db.fetchHendelser("3000", 0, 0, 2).get(0);

        assertThat(expectedHendelse, samePropertyValuesAs(hendelse1));
        assertThat(expectedHendelse, samePropertyValuesAs(hendelse2));
    }

    @Test
    public void multiple_expected_hendelser_are_fetched() {
        var expectedHendelser = testData.hendelser("01016800000", "01016900000", "01017000000");
        var hendelser = db.fetchHendelser("4000", 0, 0, 3);

        for (int i = 0; i < hendelser.size(); i++)
            assertThat(hendelser.get(i), samePropertyValuesAs(expectedHendelser.get(i)));
    }

    @Test
    public void unknown_hendelse_returns_empty_list() {
        assertEquals(Collections.emptyList(), db.fetchHendelser("1234", 1, 0, 9999));
    }

    @Test
    public void page_counting() {
        assertEquals(1, db.getNumberOfPages("2000", 1, 1));
        assertEquals(3, db.getNumberOfPages("4000", 1, 1));
        assertEquals(2, db.getNumberOfPages("4000", 1, 2));
        assertEquals(1, db.getNumberOfPages("4000", 5, 2));
        assertEquals(0, db.getNumberOfPages("4000", 7, 1));
    }

    @Test
    public void hendelser_split_with_pagination() {
        var expectedPageOne = testData.hendelser("01016800000", "01016900000");
        var expectedPageTwo = testData.hendelser("01017000000");

        var pageOne = db.fetchHendelser("4000", 0, 0, 2);
        var pageTwo = db.fetchHendelser("4000", 0, 1, 2);

        for (int i = 0; i < pageOne.size(); i++)
            assertThat(pageOne.get(i), samePropertyValuesAs(expectedPageOne.get(i)));
        assertThat(pageTwo.get(0), samePropertyValuesAs(expectedPageTwo.get(0)));
    }

    @Test
    public void skip_hendelser_with_offset() {
        var expectedHendelse = testData.hendelse("01017000000");
        var hendelser = db.fetchHendelser("4000",6, 0, 3);

        assertEquals(1, hendelser.size());
        assertThat(hendelser.get(0), samePropertyValuesAs(expectedHendelse));
    }

    @Test
    public void latest_sekvensnummer_for_tpnr() {
        var expectedSekvensnummer = Integer.valueOf(6);
        var latestSekvensnummer = db.latestSekvensnummer("4000");

        assertEquals(expectedSekvensnummer, latestSekvensnummer);
    }

    @Test
    public void latest_sekvensnummer_for_tpnr_when_no_hendelser() {
        var expectedSekvensnummer = Integer.valueOf(1);
        var latestSekvensnummer = db.latestSekvensnummer("1234");

        assertEquals(expectedSekvensnummer, latestSekvensnummer);
    }
}
