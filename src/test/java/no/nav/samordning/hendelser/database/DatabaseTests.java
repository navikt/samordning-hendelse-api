package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.*;

@SpringBootTest
public class DatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private TestDataHelper testData;

    @Test
    public void fetch_valid_hendelser() {
        assertThat(testData.hendelse("0"), samePropertyValuesAs(db.fetch(0, 5, 0).get(0)));
        assertThat(testData.hendelse("1"), samePropertyValuesAs(db.fetch(0, 5, 0).get(1)));
        assertThat(testData.hendelse("2"), samePropertyValuesAs(db.fetch(0, 5, 0).get(2)));
        assertThat(testData.hendelse("3"), samePropertyValuesAs(db.fetch(0, 5, 0).get(3)));
    }

    @Test
    public void page_count() {
        assertEquals(3, db.getNumberOfPages(4));
        assertEquals(2, db.getNumberOfPages(5));
        assertEquals(10, db.getNumberOfPages(1));
    }

    @Test
    public void pagination() {
        assertThat(testData.hendelse("0"), samePropertyValuesAs(db.fetch(0, 5, 0).get(0)));
        assertTrue(db.fetch(0, 4, 0).stream().allMatch(hendelse ->
            testData.hendelseIdList("0", "1", "2", "3").contains(hendelse.getIdentifikator())));
        assertTrue(db.fetch(1, 4, 0).stream().allMatch(hendelse ->
            testData.hendelseIdList("4", "5", "6", "7").contains(hendelse.getIdentifikator())));
        assertTrue(db.fetch(2, 4, 0).stream().allMatch(hendelse ->
            testData.hendelseIdList("8", "9").contains(hendelse.getIdentifikator())));
        assertEquals(Collections.emptyList(), db.fetch(2, 5, 0));
    }

    @Test
    public void sequence_number() {
        assertThat(testData.hendelse("0"), samePropertyValuesAs(db.fetch(0, 5, 1).get(0)));
        assertThat(testData.hendelse("1"), samePropertyValuesAs(db.fetch(0, 5, 2).get(0)));
        assertThat(testData.hendelse("9"), samePropertyValuesAs(db.fetch(0, 5, 10).get(0)));
        assertTrue(db.fetch(1, 3, 3).stream().allMatch(hendelse ->
            testData.hendelseIdList("5", "6", "7").contains(hendelse.getIdentifikator())));
        assertEquals(Collections.emptyList(), db.fetch(0, 5, 11));
    }
}
