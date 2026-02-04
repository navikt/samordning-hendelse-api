package no.nav.samordning.hendelser.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("vault.postgres")
class VaultProperties(
    val datasource: Datasource,
    val backend: String,
    val role: String
) {
    class Datasource(
        val url: String,
        val maxPoolSize: Int
    )
}
