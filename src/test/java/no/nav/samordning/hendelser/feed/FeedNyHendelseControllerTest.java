package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FeedNyHendelseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        this.mockMvc.perform(get("/hendelser?side=1")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void serviceShouldRequirePageParameter() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        this.mockMvc.perform(get("/hendelser")).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void serviceShouldntAcceptTooLargeRequests() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "1")
                .param("antall", "10001"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Man kan ikke be om flere enn 10000 hendelser.")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithFirstRecord() throws Exception {
        this.mockMvc.perform(get("/hendelser?side=1&antall=1")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].fom").value("2080-01-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].ytelsesType").value("AAP"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator").value("10000000001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].vedtakId").value("A1B2C3"));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithBasicFilter() throws Exception {

        List<String> excpected = new ArrayList<>();

        for(int i=0; i<2; i++) {
            excpected.add("2040-01-01");
        }

        this.mockMvc.perform(get("/hendelser?side=2&antall=20")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[?(@.fom=='2040-01-01')].fom").value(excpected));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithSizeCheck() throws Exception {
        this.mockMvc.perform(get("/hendelser?side=0&antall=20")
                .with(user("srvTest")))
                .andExpect(status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize(20)));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceAndReceiveURL() throws Exception {
        this.mockMvc.perform(get("/hendelser?side=2&antall=20")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status()
                        .isOk());
    }
}
