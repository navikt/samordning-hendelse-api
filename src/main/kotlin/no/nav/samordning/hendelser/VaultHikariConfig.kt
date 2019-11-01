package no.nav.samordning.hendelser

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.vault.core.lease.SecretLeaseContainer
import org.springframework.vault.core.lease.domain.RequestedSecret
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent

@Configuration
@Profile("!test")
class VaultHikariConfig(private val container: SecretLeaseContainer, private val hikariDataSource: HikariDataSource) : InitializingBean {

    @Value("\${VAULT_POSTGRES_BACKEND}")
    private lateinit var vaultPostgresBackend: String
    @Value("\${VAULT_POSTGRES_ROLE}")
    private lateinit var vaultPostgresRole: String

    override fun afterPropertiesSet() {
        val secret = RequestedSecret.rotating("$vaultPostgresBackend/creds/$vaultPostgresRole")
        container.addLeaseListener { leaseEvent ->
            if (leaseEvent.source === secret && leaseEvent is SecretLeaseCreatedEvent) {
                LOGGER.info("Rotating creds for path: ${leaseEvent.getSource().path}")
                val username = leaseEvent.secrets["username"].toString()
                val password = leaseEvent.secrets["password"].toString()
                hikariDataSource.username = username
                hikariDataSource.password = password
                hikariDataSource.hikariConfigMXBean.setUsername(username)
                hikariDataSource.hikariConfigMXBean.setPassword(password)
            }
        }
        container.addRequestedSecret(secret)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(VaultHikariConfig::class.java.name)
    }
}
