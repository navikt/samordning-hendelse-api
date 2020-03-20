package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.DatabaseTestConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EmptyDatabaseTests {

    @Autowired
    private lateinit var db: Database

    @Autowired
    private lateinit var conf: DatabaseTestConfig

    @BeforeEach
    @Throws
    fun clear() = conf.emptyDatabase()

    @AfterEach
    @Throws
    fun refill() = conf.refillDatabase()

    @Test
    fun null_count_returns_0() = assertEquals(0, db.getNumberOfPages("1000", 1, 0))
}
