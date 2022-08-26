package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.hasToString
import org.junit.jupiter.api.Test
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
internal class FeedControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun greeting_should_return_message_from_service() {
        mockMvc.perform(get(GOOD_URL))
                .andDo(print()).andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun service_shouldnt_accept_too_large_requests() {
        mockMvc.perform(get("/hendelser")
                .param("tpnr", "1000")
                .param("antall", "10001"))
                .andDo(print()).andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun greeting_should_return_message_from_service_with_first_record() {
        mockMvc.perform(get("/hendelser?tpnr=1000&antall=1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator", hasToString<Any>("01016600000")))
                .andDo(print())
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun greeting_should_return_message_from_service_with_size_check() {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=0&antall=5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize<Any>(3)))
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
    }
}
