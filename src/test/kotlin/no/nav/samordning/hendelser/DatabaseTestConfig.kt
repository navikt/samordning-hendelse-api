package no.nav.samordning.hendelser

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile

/**
 * Creates and manages shared testcontainers for all tests
 */
@Configuration
class DatabaseTestConfig {

    init {
        if (uninitialized) {
            initPostgresContainer()
            initStubs()

            System.setProperty("spring.datasource.url", postgres.jdbcUrl)
            System.setProperty("spring.datasource.username", postgres.username)
            System.setProperty("spring.datasource.password", postgres.password)
            System.setProperty("TPREGISTERET_URL", wiremock.baseUrl())
        }
    }

    private class KPostgreSQLContainer(name: String) : PostgreSQLContainer<KPostgreSQLContainer>(name)

    private fun initPostgresContainer() {
        postgres = KPostgreSQLContainer("postgres")
            .withDatabaseName("samordning-hendelser")
            .withUsername("user")
            .withPassword("pass")
        postgres
            .withNetwork(Network.SHARED)
            .withNetworkAliases("samordning-hendelser")
            .withExposedPorts(5432)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("schema.sql"),
                "/docker-entrypoint-initdb.d/schema.sql"
            )
        postgres.start()
    }

    private fun initStubs() {
        wiremock = WireMockServer()
        wiremock.stubFor(
            get("/organisation")
                .withHeader("orgNr", equalTo("0000000000"))
                .withHeader("tpId", equalTo("1000"))
                .willReturn(noContent())
        )
        wiremock.stubFor(
            get("/organisation")
                .withHeader("orgNr", equalTo("4444444444"))
                .withHeader("tpId", equalTo("4000"))
                .willReturn(noContent())
        )
        wiremock.start()
    }

    fun emptyDatabase() {
        postgres.execInContainer(
            "psql",
            "-U", postgres.username,
            "-d", postgres.databaseName,
            "-c", "TRUNCATE TABLE HENDELSER",
            "-c", "ALTER SEQUENCE HENDELSER_ID_SEQ RESTART WITH 1"
        )
    }

    fun refillDatabase() {
        postgres.execInContainer(
            "psql",
            "-U", postgres.username,
            "-d", postgres.databaseName,
            "-a", "-f", "/docker-entrypoint-initdb.d/schema.sql"
        )
    }

    companion object {
        private lateinit var postgres: KPostgreSQLContainer
        private lateinit var wiremock: WireMockServer
        private val uninitialized: Boolean
            get() = !::postgres.isInitialized
    }
}
