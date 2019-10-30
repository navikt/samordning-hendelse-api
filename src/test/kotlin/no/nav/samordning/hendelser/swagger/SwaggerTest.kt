package no.nav.samordning.hendelser.swagger

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class SwaggerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun swagger_shall_include_hendelser_but_not_basicErrorController() {
        mockMvc.perform(get("/v2/api-docs"))
                .andExpect(status().isOk)
                .andExpect(content().string(SwaggerMatcher()))
    }

    @Test
    @Throws(Exception::class)
    fun swaggerUi() {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun swaggerUi_webjars() {
        mockMvc.perform(get("/webjars/springfox-swagger-ui/springfox.css?v=2.9.2"))
                .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun swaggerUi_config() {
        mockMvc.perform(get("/swagger-resources/configuration/ui"))
                .andExpect(status().isOk)
    }

    private inner class SwaggerMatcher : BaseMatcher<String>() {

        override fun matches(o: Any): Boolean {
            return validate(o as String)
        }

        override fun describeMismatch(o: Any, description: Description) {
            description.appendText(o.toString())
        }

        override fun describeTo(description: Description) {
            description.appendText("Should contain 'hendelser', but not 'basic-error-controller'")
        }

        private fun validate(swagger: String): Boolean {
            return "hendelser" in swagger && "basic-error-controller" !in swagger
        }
    }
}
