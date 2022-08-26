package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.security.support.ROLE_SAMHANDLER
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests authentication/authorization (token validation) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class FeedControllerAuthTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `Valid request is ok`() {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isOk)
    }

    @Test
    @WithMockUser
    fun `Request missing validation is forbidden`() {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isForbidden)
    }

    @Test
    fun `Missing token is unauthorized`() {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = [ROLE_SAMHANDLER])
    fun `Missing parameter is bad request`() {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isBadRequest)
    }

    companion object {
        private const val ENDPOINT = "/hendelser"
        private const val TPNR_PARAM_NAME = "tpnr"
        private const val TPNR_1 = "1000"
        private const val TPNR_2 = "4000"
    }
}
