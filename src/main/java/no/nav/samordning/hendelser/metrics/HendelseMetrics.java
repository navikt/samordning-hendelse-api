package no.nav.samordning.hendelser.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class HendelseMetrics {

    private final MeterRegistry meterRegistry;

    public HendelseMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
}