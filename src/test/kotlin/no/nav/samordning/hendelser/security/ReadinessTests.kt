package no.nav.samordning.hendelser.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReadinessTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void isAlive_is_reachable() throws Exception {
        mockMvc.perform(get("/isAlive")).andExpect(status().isOk());
    }

    @Test
    public void isReady_is_reachable() throws Exception {
        mockMvc.perform(get("/isReady")).andExpect(status().isOk());
    }

    @Test
    public void metrics_are_reachable() throws Exception {
        mockMvc.perform(get("/actuator")).andExpect(status().isOk());
    }

    @Test
    public void metrics() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }
}