package no.nav.samordning.hendelser.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppMetrics {

    private final Counter samordning_hendelser;

    public AppMetrics(MeterRegistry registry) {
        samordning_hendelser = registry.counter("samordning_hendelser");
    }

    public void hendelserTotal(int antall) {
        samordning_hendelser.increment(antall);
    }
}