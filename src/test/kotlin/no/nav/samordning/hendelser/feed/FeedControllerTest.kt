package no.nav.samordning.hendelser.feed

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.hasToString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
internal class FeedControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @ParameterizedTest(name = "Valid requests returns ok with content")
    @ValueSource(strings = [GOOD_URL, URL_WITH_YTELSE])
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `valid requests returns ok with content`(url: String) {
        mockMvc.perform(get(url))
            .andDo(print()).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
    }

    @ParameterizedTest(name = "Service should not accept too large requests")
    @ValueSource(strings = [GOOD_URL, URL_WITH_YTELSE])
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `service shouldnt accept too large requests`(url: String) {
        mockMvc.perform(
            get(url)
                .param("antall", "10001")
        )
            .andDo(print()).andExpect(status().is4xxClientError)
    }

    @ParameterizedTest(name = "Should return message from service with first record")
    @CsvSource("$GOOD_URL, 01016600000", "$URL_WITH_YTELSE, 01019000000")
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `should return message from service with first record`(url: String, expected: String) {
        mockMvc.perform(get("$url&antall=1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator", hasToString<Any>(expected)))
            .andDo(print())
    }

    @ParameterizedTest(name = "Should return message from service with size check")
    @CsvSource("/hendelser?tpnr=4000&side=0&antall=5, 3", "$URL_WITH_YTELSE&antall=5, 1")
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `should return message from service with size check`(url: String, expected: String) {
        mockMvc.perform(get(url))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize<Any>(expected.toInt())))
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun bad_parameters_return_400() {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=-1"))
            .andDo(print()).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun delete_method_is_not_allowed() {
        mockMvc.perform(delete(GOOD_URL))
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun patch_method_is_not_allowed() {
        mockMvc.perform(patch(GOOD_URL))
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun post_method_is_not_allowed() {
        mockMvc.perform(post(GOOD_URL))
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun put_method_is_not_allowed() {
        mockMvc.perform(put(GOOD_URL))
            .andExpect(status().isMethodNotAllowed)
    }

    companion object {

        private const val GOOD_URL = "/hendelser?tpnr=1000"
        private const val URL_WITH_YTELSE = "/hendelser/ytelse?tpnr=6000&ytelse=OMS"
    }
}
