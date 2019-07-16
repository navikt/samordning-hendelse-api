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
public class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void valid_token_issuer_is_authorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", TestTokenHelper.token("0000000000", true))
                .param("tpnr", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    public void invalid_token_issuer_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", TestTokenHelper.token("0000000000", false))
                .param("tpnr", "1000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void no_token_is_unauthorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .param("tpnr", "1000"))
                .andExpect(status().isUnauthorized());
    }
}
