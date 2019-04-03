package no.nav.samordning.hendelser.feed;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FeedNyHendelseControllerDatesTest {

    @Autowired
    private MockMvc mockMvc;

/*    @Test
    public void greetingShouldReturnMessageFromServiceWithSokFraBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=1&ytelsesType=AAP&sokFra=0800-12-12")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithSokFraAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=20&ytelsesType=AAP&sokFra=2101-01-01")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithSokTilBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=2&antall=50&ytelsesType=AAP&sokTil=0800-12-12")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithSokTilAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=1&antall=10&ytelsesType=AAP&sokTil=2200-12-30")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }*/

    @Test
    public void greetingShouldReturnMessageFromServiceWithValidDates() throws Exception {

        this.mockMvc.perform(get("/hendelser?side=1&antall=10&ytelsesType=AAP&sokFra=2020-01-01&sokTil=2040-01-01")
                .with(user("srvTest")))
                .andDo(print())
                .andExpect(status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize(10)));
    }
}
