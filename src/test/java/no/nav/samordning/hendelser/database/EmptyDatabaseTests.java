package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class EmptyDatabaseTests {

    @Autowired
    private Database db;

    @Autowired
    private DatabaseConfig conf;

    @BeforeEach
    public void clear() throws Exception {
        conf.emptyDatabase();
    }

    @AfterEach
    public void refill() throws Exception {
        conf.refillDatabase();
    }

    @Test
    public void null_count_returns_0() {
        assertEquals(0, db.getNumberOfPages(0));
    }
}
