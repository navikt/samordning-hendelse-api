package no.nav.samordning.hendelser;

import com.zaxxer.hikari.HikariDataSource;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HikariConfig {

    @Value("${DB_URL}")
    private String jdbcUrl;

    @Value("${DB_MOUNT_PATH}")
    private String mountPath;

    @Value("${DB_ROLE}")
    private String role;

    @Bean
    public HikariDataSource makeDataSource() throws VaultError {
        final com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setMaxLifetime(30001);
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(250);
        config.setIdleTimeout(10001);
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, role);
    }
}
