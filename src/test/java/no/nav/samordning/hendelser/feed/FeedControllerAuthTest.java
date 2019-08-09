package no.nav.samordning.hendelser.feed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static no.nav.samordning.hendelser.TestTokenHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests authentication/authorization (token validation) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FeedControllerAuthTest {

    private static final String ENDPOINT = "/hendelser";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String TPNR_PARAM_NAME = "tpnr";
    private static final String ORG_NUMBER_1 = "0000000000";
    private static final String ORG_NUMBER_2 = "4444444444";
    private static final String TPNR_1 = "1000";
    private static final String TPNR_2 = "4000";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void valid_token_issuer_is_authorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, true))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isOk());
    }

    @Test
    void valid_orgno_and_tpnr_is_authorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_2, true))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isOk());
    }

    @Test
    void expired_token_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, expiredToken(ORG_NUMBER_2))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void future_token_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, futureToken(ORG_NUMBER_2))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_token_issuer_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, false))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_orgno_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token("1111111111", false))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_tpnr_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, false))
                .param(TPNR_PARAM_NAME, "1235"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_orgno_and_tpnr_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token("9999999999", true))
                .param(TPNR_PARAM_NAME, "2000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void no_token_is_unauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missing_required_parameter_returns_bad_request() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, emptyToken())
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isBadRequest());
    }
}
