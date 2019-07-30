package no.nav.samordning.hendelser.metrics;

import no.nav.samordning.hendelser.TestTokenHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppMetricsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void rejected_request_increments_counter() throws Exception {
        var before = getMetricValue("rejected_hendelser_requests");

        mockMvc.perform(get("/hendelser"))
                .andExpect(status().isUnauthorized());

        assertEquals(before + 1.0, getMetricValue("rejected_hendelser_requests"));
    }

    @Test
    public void accepted_request_increments_counter() throws Exception {
        var before = getMetricValue("accepted_hendelser_requests");

        mockMvc.perform(get("/hendelser")
                .header("Authorization", TestTokenHelper.srvToken())
                .param("tpnr", "1000"))
                .andExpect(status().isOk());

        assertEquals(before + 1.0, getMetricValue("accepted_hendelser_requests"));
    }

    private double getMetricValue(String metricName) throws Exception {
        var response = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn().getResponse().getContentAsString().split("\n");

        for (var line : response) {
            if (line.startsWith(metricName)) {
                var startIndex = metricName.length() + "_total".length() + 1;
                return Double.parseDouble(line.substring(startIndex, startIndex + 3));
            }
        }

        return Double.MIN_VALUE;
    }
}
