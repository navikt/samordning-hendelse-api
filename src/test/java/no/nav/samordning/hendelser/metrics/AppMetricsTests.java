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
    public void hendelser_total() throws Exception {
        var before = getMetricValue("samordning_hendelser_total");

        mockMvc.perform(get("/hendelser")
                .header("Authorization", TestTokenHelper.srvToken())
                .param("tpnr", "4000"))
                .andExpect(status().isOk());

        assertEquals(before + 3, getMetricValue("samordning_hendelser_total"));
    }

    private double getMetricValue(String metricName) throws Exception {
        var response = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn().getResponse().getContentAsString().split("\n");

        for (var line : response) {
            if (line.startsWith(metricName)) {
                var startIndex = metricName.length() + 1;
                return Double.parseDouble(line.substring(startIndex, startIndex + 3));
            }
        }

        return Double.MIN_VALUE;
    }
}
