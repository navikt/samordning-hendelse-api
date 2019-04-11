package no.nav.samordning.hendelser.hendelse;

import no.nav.samordning.hendelser.TestDataHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private TestDataHelper testData;

    @Test
    public void fetch_test() {
        assertThat(testData.hendelse("0"), samePropertyValuesAs(db.fetch(0, 5).get(0)));
    }

    @Test
    public void count_test() {
        assertEquals(3, db.getNumberOfPages(4));
    }
}
