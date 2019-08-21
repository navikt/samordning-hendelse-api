package no.nav.samordning.hendelser.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.samordning.hendelser.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

@Component
public class AppMetrics {

    @Autowired
    private Database database;

    private Gauge samordning_hendelser;
    private Number totalAntallHendelser = 0;

    public AppMetrics(MeterRegistry registry) {
        samordning_hendelser = Gauge.builder("samordning_hendelser_total", () -> totalAntallHendelser).register(registry);
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
}