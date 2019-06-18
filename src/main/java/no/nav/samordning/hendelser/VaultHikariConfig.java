package no.nav.samordning.hendelser;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.vault.core.lease.LeaseEndpoints;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;

@Configuration
@Profile("!test")
public class VaultHikariConfig implements InitializingBean {

    private SecretLeaseContainer container;
    private HikariDataSource hikariDataSource;

    @Value("${VAULT_POSTGRES_BACKEND}")
    private String vaultPostgresBackend;
    @Value("${VAULT_POSTGRES_ROLE}")
    private String vaultPostgresRole;

    private static Logger LOGGER = LoggerFactory.getLogger(VaultHikariConfig.class.getName());

    public VaultHikariConfig(SecretLeaseContainer container, HikariDataSource hikariDataSource) {
        this.container = container;
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public void afterPropertiesSet() {
        container.setLeaseEndpoints(LeaseEndpoints.SysLeases); // TODO: Remove
        RequestedSecret secret = RequestedSecret.rotating(this.vaultPostgresBackend + "/creds/" + this.vaultPostgresRole);
        container.addLeaseListener(leaseEvent -> {
            if (leaseEvent.getSource() == secret && leaseEvent instanceof SecretLeaseCreatedEvent) {
                LOGGER.info("Rotating creds for path: " + leaseEvent.getSource().getPath());
                SecretLeaseCreatedEvent slce = (SecretLeaseCreatedEvent) leaseEvent;
                String username = slce.getSecrets().get("username").toString();
                String password = slce.getSecrets().get("password").toString();
                hikariDataSource.setUsername(username);
                hikariDataSource.setPassword(password);
                hikariDataSource.getHikariConfigMXBean().setUsername(username);
                hikariDataSource.getHikariConfigMXBean().setPassword(password);
            }
        });
        container.addRequestedSecret(secret);
    }
}
