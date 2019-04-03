package no.nav.samordning.hendelser.hendelse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseTests {

    @Autowired
    private Database db;

    @Test
    public void fetchTest() {
        Hendelse expected = new Hendelse();
        expected.setYtelsesType("AAP");
        expected.setIdentifikator("12345678901");
        expected.setVedtakId("ABC123");
        expected.setFom(LocalDate.of(2020, 01, 01));
        assertThat(expected, samePropertyValuesAs(db.fetch(0, 20).get(0)));
    }

    @Test
    public void countTest() {
        int result = db.getNumberOfPages();
        assertEquals(result, 3);
    }
}
