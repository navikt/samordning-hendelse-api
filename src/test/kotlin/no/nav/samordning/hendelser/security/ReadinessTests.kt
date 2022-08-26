package no.nav.samordning.hendelser.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMetrics
@AutoConfigureMockMvc
class ReadinessTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun isAlive_is_reachable() {
        mockMvc.perform(get("/isAlive")).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun isReady_is_reachable() {
        mockMvc.perform(get("/isReady")).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun metrics_are_reachable() {
        mockMvc.perform(get("/actuator")).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun metrics() {
        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isOk)
    }
}
