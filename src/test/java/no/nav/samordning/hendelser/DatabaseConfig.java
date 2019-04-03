package no.nav.samordning.hendelser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@Component
public class DatabaseConfig {
    private static PostgreSQLContainer postgres;

    @Autowired(required = true)
    public void postgres() {
        postgres = new PostgreSQLContainer()
            .withUsername("user")
            .withPassword("pass");
        postgres.withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
            "/docker-entrypoint-initdb.d/");
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
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
