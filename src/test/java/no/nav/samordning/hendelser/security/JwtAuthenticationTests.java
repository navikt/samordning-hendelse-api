package no.nav.samordning.hendelser.security;

import no.nav.samordning.hendelser.TestToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_correct_credentials_authenticated() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestToken.getValidAccessToken())
                .param("side", "1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void test_wrong_credentials_unauthenticated() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", TestToken.getInvalidAccessToken())
                .param("side", "1")).andDo(print()).andExpect(status().isUnauthorized());
    }
}
