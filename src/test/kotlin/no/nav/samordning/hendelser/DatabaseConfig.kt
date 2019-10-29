package no.nav.samordning.hendelser;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.security.NoSuchAlgorithmException;

/**
 * Creates and manages shared testcontainers for all tests
 */
@Component
public class DatabaseConfig {

    private static PostgreSQLContainer postgres;
    private static MockServerContainer mockServer;

    @Autowired
    public void init() throws Exception {
        if (postgres == null && mockServer == null) {
            initPostgresContainer();
            initMockServerContainer();

            System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgres.getUsername());
            System.setProperty("spring.datasource.password", postgres.getPassword());
            System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", mockServer.getEndpoint() + "/jwks");
            System.setProperty("TPREGISTERET_URL", mockServer.getEndpoint());
        }
    }

    private void initPostgresContainer() {
        postgres = new PostgreSQLContainer()
            .withDatabaseName("samordning-hendelser")
            .withUsername("user")
            .withPassword("pass");
        postgres
            .withNetwork(Network.SHARED)
            .withNetworkAliases("samordning-hendelser")
            .withExposedPorts(5432)
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                "/docker-entrypoint-initdb.d/schema.sql");
        postgres.start();
    }

    private void initMockServerContainer() throws NoSuchAlgorithmException {
        mockServer = new MockServerContainer();
        mockServer.start();

        var mockClient = new MockServerClient(mockServer.getContainerIpAddress(), mockServer.getServerPort());

        mockClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/jwks"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader("\"Content-type\", \"application/json\"")
                .withBody(TestTokenHelper.generateJwks())
            );

        mockClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/organisation")
                .withHeader("orgnr", "0000000000")
                .withHeader("tpnr", "1000"))
                .respond(HttpResponse.response()
                .withStatusCode(200));

        mockClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/organisation")
                .withHeader("orgnr", "4444444444")
                .withHeader("tpnr", "4000"))
                .respond(HttpResponse.response()
                .withStatusCode(200));
    }

    public void emptyDatabase() throws Exception {
        postgres.execInContainer("psql",
            "-U", postgres.getUsername(),
            "-d", postgres.getDatabaseName(),
            "-c", "TRUNCATE TABLE HENDELSER",
            "-c", "ALTER SEQUENCE HENDELSER_ID_SEQ RESTART WITH 1");
    }

    public void refillDatabase() throws Exception {
        postgres.execInContainer("psql",
            "-U", postgres.getUsername(),
            "-d", postgres.getDatabaseName(),
            "-a", "-f", "/docker-entrypoint-initdb.d/schema.sql");
    }
}
