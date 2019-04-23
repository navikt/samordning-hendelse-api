package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.DatabaseConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EmptyDatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private DatabaseConfig conf;

    @Before
    public void clear() throws Exception {
        conf.emptyDatabase();
    }

    @After
    public void refill() throws Exception {
        conf.refillDatabase();
    }

    @Test
    public void null_count_returns_0() {
        assertEquals(0, db.getNumberOfPages(0));
    }
}
