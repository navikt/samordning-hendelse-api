package no.nav.samordning.hendelser.swagger;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swagger_shall_include_hendelser_but_not_basicErrorController() throws Exception {
        mockMvc.perform(get("/v2/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(new SwaggerMatcher()));
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

    private class SwaggerMatcher extends BaseMatcher<String> {

        @Override
        public boolean matches(Object o) {
            return validate((String) o);
        }

        @Override
        public void describeMismatch(Object o, Description description) {
            description.appendText(o.toString());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Should contain 'hendelser', but not 'basic-error-controller'");
        }

        private boolean validate(String swagger) {
            return swagger.contains("hendelser") && !swagger.contains("basic-error-controller");
        }
    }
}
