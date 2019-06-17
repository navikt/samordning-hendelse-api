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

/**
 * Creates and manages shared testcontainers for all tests
 */
@Component
public class DatabaseConfig {

    private static PostgreSQLContainer postgres;
    private static MockServerContainer mockServer;

    @Autowired
    public void init() {
        if (postgres == null && mockServer == null) {
            initPostgresContainer();
            initMockServerContainer();

            System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgres.getUsername());
            System.setProperty("spring.datasource.password", postgres.getPassword());
            System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", mockServer.getEndpoint() + "/jwks");
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

    private void initMockServerContainer() {
        mockServer = new MockServerContainer();
        mockServer.start();
        new MockServerClient(mockServer.getContainerIpAddress(), mockServer.getServerPort())
            .when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/jwks"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withHeader("\"Content-type\", \"application/json\"")
                .withBody("{\n" +
                    "  \"keys\": [{\n" +
                    "      \"kty\": \"RSA\",\n" +
                    "      \"e\": \"AQAB\",\n" +
                    "      \"use\": \"sig\",\n" +
                    "      \"n\": \"33TqqLR3eeUmDtHS89qF3p4MP7Wfqt2Zjj3lZjLjjCGDvwr9cJNlNDiuKboODgUiT4ZdPWbOiMAfDcDzlOxA04DDnEFGAf-kDQiNSe2ZtqC7bnIc8-KSG_qOGQIVaay4Ucr6ovDkykO5Hxn7OU7sJp9TP9H0JH8zMQA6YzijYH9LsupTerrY3U6zyihVEDXXOv08vBHk50BMFJbE9iwFwnxCsU5-UZUZYw87Uu0n4LPFS9BT8tUIvAfnRXIEWCha3KbFWmdZQZlyrFw0buUEf0YN3_Q0auBkdbDR_ES2PbgKTJdkjc_rEeM0TxvOUf7HuUNOhrtAVEN1D5uuxE1WSw\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}")
            );
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
