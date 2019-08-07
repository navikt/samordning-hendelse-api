package no.nav.samordning.hendelser.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static no.nav.samordning.hendelser.TestTokenHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenResolverTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void valid_orgno_and_tpnr_is_authorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", token("4444444444", true))
                .param("tpnr", "4000"))
                .andExpect(status().isOk());
    }

    @Test
    void expired_token_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", expiredToken("4444444444"))
                .param("tpnr", "4000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void future_token_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", futureToken("4444444444"))
                .param("tpnr", "4000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_orgno_and_tpnr_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", token("9999999999", true))
                .param("tpnr", "4000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missing_required_parameter_returns_bad_request() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", emptyToken())
                .param("tpnr", "2000"))
                .andExpect(status().isBadRequest());
    }
}
