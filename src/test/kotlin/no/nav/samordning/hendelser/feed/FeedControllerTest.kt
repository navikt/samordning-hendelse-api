package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.TestAuthHelper.token
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.hasToString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.NoSuchAlgorithmException

@SpringBootTest
@AutoConfigureMockMvc
internal class FeedControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service() {
        mockMvc.perform(get(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andDo(print()).andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    @Test
    @Throws(Exception::class)
    fun service_shouldnt_accept_too_large_requests() {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", prepareToken())
                .param("tpnr", "1000")
                .param("antall", "10001"))
                .andDo(print()).andExpect(status().is4xxClientError)
    }

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service_with_first_record() {
        mockMvc.perform(get("/hendelser?tpnr=1000&antall=1")
                .header("Authorization", prepareToken()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator", hasToString<Any>("01016600000")))
                .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service_with_size_check() {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=0&antall=5")
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize<Any>(3)))
    }

    @Test
    @Throws(Exception::class)
    fun bad_parameters_return_400() {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=-1")
                .header("Authorization", token("4444444444", true)))
                .andDo(print()).andExpect(status().isBadRequest)
    }

    @Test
    @Throws(Exception::class)
    fun delete_method_is_not_allowed() {
        mockMvc.perform(delete(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @Throws(Exception::class)
    fun patch_method_is_not_allowed() {
        mockMvc.perform(patch(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @Throws(Exception::class)
    fun post_method_is_not_allowed() {
        mockMvc.perform(post(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    @Throws(Exception::class)
    fun put_method_is_not_allowed() {
        mockMvc.perform(put(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed)
    }

    companion object {

        private const val GOOD_URL = "/hendelser?tpnr=1000"

        @Throws(NoSuchAlgorithmException::class)
        private fun prepareToken(): String {
            return token("0000000000", true)
        }
    }
}
