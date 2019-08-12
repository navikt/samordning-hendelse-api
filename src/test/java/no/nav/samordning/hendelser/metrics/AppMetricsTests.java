package no.nav.samordning.hendelser.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static no.nav.samordning.hendelser.TestAuthHelper.serviceToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppMetricsTests {

    private static final String METRIC_NAME = "samordning_hendelser_total";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hendelser_total() throws Exception {
        var before = getMetricValue();

        mockMvc.perform(get("/hendelser")
                .header("Authorization", serviceToken())
                .param("tpnr", "4000"))
                .andExpect(status().isOk());

        assertEquals(before + 3, getMetricValue());
    }

    private double getMetricValue() throws Exception {
        var response = mockMvc.perform(get("/actuator/prometheus"))
                .andReturn().getResponse().getContentAsString().split("\n");

        for (var line : response) {
            if (line.startsWith(AppMetricsTests.METRIC_NAME)) {
                var startIndex = AppMetricsTests.METRIC_NAME.length() + 1;
                return Double.parseDouble(line.substring(startIndex, startIndex + 3));
            }
        }

        return Double.MIN_VALUE;
    }
}
