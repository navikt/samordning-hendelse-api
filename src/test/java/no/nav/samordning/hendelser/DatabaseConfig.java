package no.nav.samordning.hendelser;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

/**
 * Creates and manages shared testcontainers between all tests to mock database/vault/STS
 */
@Component
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static PostgreSQLContainer postgres;
    private static GenericContainer vault;
    private static MockServerContainer mockServer;

    @Autowired
    public void init() {
        if (postgres == null && vault == null) {
            setupPostgresContainer();
            setupVaultContainer();
        }
        if (mockServer == null) setupMockServer();
    }

    private void setupPostgresContainer() {
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

    private void setupVaultContainer() {
        vault = new GenericContainer(
            new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder ->
                    builder
                        .from("vault:1.1.0")
                        .env("DB_NAME", postgres.getDatabaseName())
                        .env("DB_USERNAME", postgres.getUsername())
                        .env("DB_PASSWORD", postgres.getPassword())
                        .env("VAULT_ADDR", "http://localhost:8200")
                        .env("VAULT_DEV_ROOT_TOKEN_ID", "secret")
                        .env("VAULT_TOKEN", "secret")
                        .build()))
            .withNetwork(Network.SHARED)
            .withExposedPorts(8200)
            .withNetworkAliases("vault")
            .withCopyFileToContainer(MountableFile.forClasspathResource("vault_setup.sh"), "/vault_setup.sh");
        vault.start();

        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_MOUNT_PATH", "secrets/test");
        System.setProperty("DB_ROLE", "user");
        System.setProperty("VAULT_TOKEN", "secret");
        System.setProperty("VAULT_ADDR", String.format("http://%s:%d",
            vault.getContainerIpAddress(), vault.getMappedPort(8200)));

        try {
            vault.execInContainer("sh", "vault_setup.sh");
        } catch (Exception e) {
            logger.error("Failed to setup vault testcontainer", e);
        }
    }

    private void setupMockServer() {
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

        System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", mockServer.getEndpoint() + "/jwks");
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
