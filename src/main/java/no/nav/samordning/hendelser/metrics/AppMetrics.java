package no.nav.samordning.hendelser.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.samordning.hendelser.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AppMetrics {

    private static final Logger LOG = LoggerFactory.getLogger(AppMetrics.class);

    @Autowired
    private Database database;

    private Number totalAntallHendelser = 0;

    private Map<String, Counter> hendelserLestCounterList = new HashMap<>();

    public AppMetrics(MeterRegistry registry) {
        Gauge.builder("samordning_hendelser_total", () -> totalAntallHendelser).register(registry);
        hendelserLestCounterList.put("3010", Counter.builder("samordning_hendelser_lest").tag("tpnr", "3010").register(registry));
    }

    @Bean
    public void totalHendelserCount() {
        TimerTask counterTask = new TimerTask() {
            @Override
            public void run() {
                totalAntallHendelser = Integer.parseInt(database.getTotalHendelser());
            }
        };
        Timer timer = new Timer("Timer");
        long delay = 1000L * 60;
        long period = 1000L * 60;
        timer.scheduleAtFixedRate(counterTask, delay, period);
    }

    public void incHendelserLest(String tpnr, double antall) {
        try {
            hendelserLestCounterList.get(tpnr).increment(antall);
        } catch (NullPointerException e) {
            LOG.info("No counter for tpnr: " + tpnr);
        }
    }
}