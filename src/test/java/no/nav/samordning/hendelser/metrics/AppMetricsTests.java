package no.nav.samordning.hendelser.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static no.nav.samordning.hendelser.TestAuthHelper.token;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppMetricsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hendelser_lest_metrics_are_incremented() throws Exception {
        mockMvc.perform(get("/hendelser?tpnr=1000")
                .header("Authorization", token("0000000000",true)))
                .andExpect(status().isOk());

        var metricName = "samordning_hendelser_lest_total{tpnr=\"1000\",}";
        assertNotNull(getMetricValue(metricName));
        var count = getMetricValue(metricName);

        mockMvc.perform(get("/hendelser?tpnr=1000")
                .header("Authorization", token("0000000000",true)))
                .andExpect(status().isOk());

        assertEquals(count + 1, getMetricValue(metricName));
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
