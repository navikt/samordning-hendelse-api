package no.nav.samordning.hendelser;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class Metrics {

    private final MeterRegistry registry;

    private static Counter getRequests;
    private static Counter postRequests;
    private static Counter antallHendelser;

    @Autowired
    public Metrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void setup() {
        getRequests = Counter.builder("sam_he_get_requests")
                .description("Total antall GET requests mot hendelsesfeeden")
                .register(registry);

        postRequests = Counter.builder("sam_he_post_requests")
                .description("Total antall POST requests mot hendelsesfeeden")
                .register(registry);

        antallHendelser = Counter.builder("sam_he_antall_hendelser")
                .description("Total antall hendelser prosessert")
                .register(registry);
    }

    public static void incGetRequests() {
        getRequests.increment();
    }

    public static void incPostRequests() {
        postRequests.increment();
    }

    public static void incAntallHendelser() {
        antallHendelser.increment();
    }
}