package no.nav.samordning.hendelser.common.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@EnableConfigurationProperties(VaultProperties::class)
@Profile("!test")
class VaultHikariConfig(
    private val vault: VaultProperties
) {

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = vault.datasource.url
        minimumIdle = 1
        maxLifetime = 30001
        maximumPoolSize = vault.datasource.maxPoolSize
        connectionTimeout = 250
        idleTimeout = 10001
    }

    @Bean
    fun hikariDataSource(): HikariDataSource = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
        hikariConfig,
        vault.backend,
        vault.role
    )
}
