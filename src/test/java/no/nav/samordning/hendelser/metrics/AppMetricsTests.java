package no.nav.samordning.hendelser.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class AppMetricsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void leste_hendelser() throws Exception {

    }

    private Double getMetricValue(String metric) throws Exception {
        var response = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn().getResponse().getContentAsString().split("\n");

        for (var line : response) {
            if (line.contains("#"))
                continue;

            if (line.startsWith(metric)) {
                return Double.parseDouble(line.split(" ")[1]);
            }
        }

        return null;
    }
}
