package no.nav.samordning.hendelser.metrics

import no.nav.samordning.hendelser.TestAuthHelper.token
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class AppMetricsTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun hendelser_lest_metrics_are_incremented() {
        mockMvc.perform(get("/hendelser?tpnr=1000")
                .header("Authorization", token("0000000000", true)))
                .andExpect(status().isOk)

        val metricName = "samordning_hendelser_lest_total{tpnr=\"1000\",}"
        assertNotNull(getMetricValue(metricName))
        val count = getMetricValue(metricName)

        mockMvc.perform(get("/hendelser?tpnr=1000")
                .header("Authorization", token("0000000000", true)))
                .andExpect(status().isOk)

        assertEquals(count!! + 1, getMetricValue(metricName))
    }

    @Throws(Exception::class)
    private fun getMetricValue(metric: String) = mockMvc.perform(get("/actuator/prometheus"))
            .andReturn().response.contentAsString.split('\n')
            .firstOrNull { it.startsWith(metric) && '#' !in it }
            ?.split(' ')
            ?.filter(String::isNotEmpty)
            ?.getOrNull(1)
            ?.toDouble()
}
