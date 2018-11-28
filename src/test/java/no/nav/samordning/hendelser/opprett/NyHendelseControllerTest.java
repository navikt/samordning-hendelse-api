package no.nav.samordning.hendelser.opprett;


import no.nav.samordning.hendelser.hendelse.Database;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(NyHendelseController.class)
public class NyHendelseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Database service;


    @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        var req = "{\"ytelsesType\":null,\"identifikator\":null,\"vedtakId\":\"1234\",\"fom\":null,\"tom\":null}";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/hendelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
