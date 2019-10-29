package no.nav.samordning.hendelser

import org.mockserver.client.server.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import java.security.NoSuchAlgorithmException

/**
 * Creates and manages shared testcontainers for all tests
 */
@Component
class DatabaseConfig {

    @Autowired
    @Throws(Exception::class)
    fun init() {
        if (postgres == null && mockServer == null) {
            initPostgresContainer()
            initMockServerContainer()

            System.setProperty("spring.datasource.url", postgres!!.jdbcUrl)
            System.setProperty("spring.datasource.username", postgres!!.username)
            System.setProperty("spring.datasource.password", postgres!!.password)
            System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", mockServer!!.endpoint + "/jwks")
            System.setProperty("TPREGISTERET_URL", mockServer!!.endpoint)
        }
    }

    private class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

    private fun initPostgresContainer() {
        postgres = KPostgreSQLContainer()
                .withDatabaseName("samordning-hendelser")
                .withUsername("user")
                .withPassword("pass")
        postgres!!
                .withNetwork(Network.SHARED)
                .withNetworkAliases("samordning-hendelser")
                .withExposedPorts(5432)
                .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                        "/docker-entrypoint-initdb.d/schema.sql")
        postgres!!.start()
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun initMockServerContainer() {
        mockServer = MockServerContainer()
        mockServer!!.start()

        val mockClient = MockServerClient(mockServer!!.containerIpAddress, mockServer!!.serverPort!!)

        mockClient.`when`(HttpRequest.request()
                .withMethod("GET")
                .withPath("/jwks"))
                .respond(HttpResponse.response()
                        .withStatusCode(200)
                        .withHeader("\"Content-type\", \"application/json\"")
                        .withBody(TestTokenHelper.generateJwks())
                )

        mockClient.`when`(HttpRequest.request()
                .withMethod("GET")
                .withPath("/organisation")
                .withHeader("orgnr", "0000000000")
                .withHeader("tpnr", "1000"))
                .respond(HttpResponse.response()
                        .withStatusCode(200))

        mockClient.`when`(HttpRequest.request()
                .withMethod("GET")
                .withPath("/organisation")
                .withHeader("orgnr", "4444444444")
                .withHeader("tpnr", "4000"))
                .respond(HttpResponse.response()
                        .withStatusCode(200))
    }

    @Throws(Exception::class)
    fun emptyDatabase() {
        postgres!!.execInContainer("psql",
                "-U", postgres!!.username,
                "-d", postgres!!.databaseName,
                "-c", "TRUNCATE TABLE HENDELSER",
                "-c", "ALTER SEQUENCE HENDELSER_ID_SEQ RESTART WITH 1")
    }

    @Throws(Exception::class)
    fun refillDatabase() {
        postgres!!.execInContainer("psql",
                "-U", postgres!!.username,
                "-d", postgres!!.databaseName,
                "-a", "-f", "/docker-entrypoint-initdb.d/schema.sql")
    }

    companion object {

        private var postgres: KPostgreSQLContainer? = null
        private var mockServer: MockServerContainer? = null
    }
}
