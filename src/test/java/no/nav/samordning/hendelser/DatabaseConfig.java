package no.nav.samordning.hendelser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

@Component
public class DatabaseConfig {
    private static PostgreSQLContainer postgres;
    private static GenericContainer vault;
    private static Network network = Network.SHARED;

    private String DATABASE_NAME = "samordning-hendelser";
    private String DATABASE_USERNAME = "username";
    private String DATABASE_PASSWORD = "password";

    @Autowired
    public void setup() throws Exception {
        postgres = new PostgreSQLContainer()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);
        postgres
            .withNetwork(network)
            .withNetworkAliases(DATABASE_NAME)
            .withExposedPorts(5432)
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
            "/docker-entrypoint-initdb.d/");
        postgres.start();

        vault = new GenericContainer(
            new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder ->
                    builder
                        .from("vault:1.1.0")
                        .env("DB_NAME", DATABASE_NAME)
                        .env("DB_USERNAME", DATABASE_USERNAME)
                        .env("DB_PASSWORD", DATABASE_PASSWORD)
                        .env("VAULT_ADDR", "http://localhost:8200")
                        .env("VAULT_DEV_ROOT_TOKEN_ID", "secret")
                        .env("VAULT_TOKEN", "secret")
                        .build()))
            .withNetwork(network)
            .withExposedPorts(8200)
            .withNetworkAliases("vault")
            .withCopyFileToContainer(MountableFile.forClasspathResource("policy_db.hcl"), "/")
            .withCopyFileToContainer(MountableFile.forClasspathResource("vault_setup.sh"), "/");
        vault.start();

        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_MOUNT_PATH", "secrets/test");
        System.setProperty("DB_ROLE", "user");
        System.setProperty("VAULT_ADDR", String.format("http://%s:%d",
            vault.getContainerIpAddress(), vault.getMappedPort(8200)));
        System.setProperty("VAULT_TOKEN", "secret");

        vault.execInContainer("sh", "vault_setup.sh");
    }

    public void emptyDatabase() throws Exception {
        postgres.execInContainer("psql",
            "-U", postgres.getUsername(),
            "-d", postgres.getDatabaseName(),
            "-c", "TRUNCATE TABLE T_SAMORDNINGSPLIKTIG_VEDTAK");
    }

    public void refillDatabase() throws Exception {
        postgres.execInContainer("psql",
            "-U", postgres.getUsername(),
            "-d", postgres.getDatabaseName(),
            "-a", "-f", "/docker-entrypoint-initdb.d/schema.sql");
    }
}
