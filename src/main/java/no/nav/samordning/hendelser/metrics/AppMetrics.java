package no.nav.samordning.hendelser.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppMetrics {

    private final Counter rejectedRequests;
    private final Counter acceptedRequests;
    private final Counter hendelserTotal;

    public AppMetrics(MeterRegistry registry) {
        rejectedRequests = registry.counter("rejected_hendelser_requests");
        acceptedRequests = registry.counter("accepted_hendelser_requests");
        hendelserTotal = registry.counter("hendelser_total");
    }

    public void rejectRequest() {
        rejectedRequests.increment();
    }

    public void acceptRequest() {
        acceptedRequests.increment();
    }
}