package no.nav.samordning.hendelser

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class VaultHikariConfig(
    @Value("\${SPRING_DATASOURCE_URL}")
    private val vaultPostgresUrl: String,
    @Value("\${VAULT_POSTGRES_BACKEND}")
    private val vaultPostgresBackend: String,
    @Value("\${VAULT_POSTGRES_ROLE}")
    private val vaultPostgresRole: String,
    @Value("\${MAX_POOL_SIZE}")
    private val maxPoolSize: Int
) {

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = vaultPostgresUrl
        minimumIdle = 0
        maxLifetime = 30001
        maximumPoolSize = maxPoolSize
        connectionTimeout = 250
        idleTimeout = 10001
    }

    @Bean
    fun hikariDataSource(): HikariDataSource = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
        hikariConfig,
        vaultPostgresBackend,
        vaultPostgresRole
    )
}
