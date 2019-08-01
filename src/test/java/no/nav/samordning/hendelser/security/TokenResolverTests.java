package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.TestTokenHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenResolverTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void valid_orgno_and_tpnr_is_authorized() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", TestTokenHelper.token("4444444444", true))
                .param("tpnr", "4000"))
                .andExpect(status().isOk());
    }

//    @Test
//    public void invalid_orgno_and_tpnr_is_unauthorized() throws Exception {
//        mockMvc.perform(get("/hendelser")
//                .header("Authorization", TestTokenHelper.token("9999999999", true))
//                .param("tpnr", "4000"))
//                .andExpect(status().isUnauthorized());
//    }

//    @Test
//    public void missing_required_parameter_returns_bad_request() throws Exception {
//        mockMvc.perform(get("/hendelser")
//                .header("Authorization", TestTokenHelper.emptyToken())
//                .param("tpnr", "2000"))
//                .andExpect(status().isBadRequest());
//    }
}
