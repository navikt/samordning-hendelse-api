package no.nav.samordning.hendelser.hendelse;

import no.nav.samordning.hendelser.DatabaseConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmptyDatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private DatabaseConfig conf;

    @Before
    public void setup() throws Exception {
        conf.emptyDatabase();
    }

    @After
    public void cleanup() throws Exception {
        conf.refillDatabase();
    }

    @Test
    public void null_count_returns_0() {
        var count = db.getNumberOfPages();
        assertEquals(0, count);
    }
}
