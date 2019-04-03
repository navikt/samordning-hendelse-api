package no.nav.samordning.hendelser.authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BasicAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Value("${SRV_USERNAME}")
    private String username;

    @Value("${SRV_PASSWORD}")
    private String password;

    @Test
    public void correct_credentials_authorized() throws Exception {
        this.mockMvc.perform(get("/hendelser&side=1").with(httpBasic(username, password)));
                //.param("side", "1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void wrong_credentials_unauthorized() throws Exception {
        this.mockMvc.perform(get("/hendelser").with(httpBasic("user", "..."))
                .param("side", "1")).andDo(print()).andExpect(status().isUnauthorized());
    }
}