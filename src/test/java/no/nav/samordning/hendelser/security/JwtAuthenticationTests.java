package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.TestTokenHelper;
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
public class JwtAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void valid_token_issuer_is_authorized() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestTokenHelper.getValidAccessToken())
                .param("tpnr", "1000")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void invalid_token_issuer_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestTokenHelper.getInvalidAccessToken())
                .param("tpnr", "1000")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void valid_token_and_valid_claims_is_authorized() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestTokenHelper.getInvalidAccessToken())
            .param("tpnr", "1000")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    public void valid_token_issuer_and_invalid_claims_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestTokenHelper.getInvalidAccessToken())
                .param("tpnr", "1000")).andDo(print()).andExpect(status().isUnauthorized());
    }
}
