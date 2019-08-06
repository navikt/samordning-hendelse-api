package no.nav.samordning.hendelser.swagger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swagger() throws Exception {
        mockMvc.perform(get("/v2/api-docs"))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    void swaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUi_webjars() throws Exception {
        mockMvc.perform(get("/webjars/springfox-swagger-ui/springfox.css?v=2.9.2"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUi_config() throws Exception {
        mockMvc.perform(get("/swagger-resources/configuration/ui"))
                .andExpect(status().isOk());
    }
}
