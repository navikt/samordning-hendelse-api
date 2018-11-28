package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Database;
import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@WebMvcTest(FeedController.class)
public class FeedNyHendelseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Database service;


   @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        when(service.fetchAll()).thenReturn(List.of(hendelse));

        this.mockMvc.perform(get("/hendelser")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"hendelser\":[{\"ytelsesType\":null,\"identifikator\":null,\"vedtakId\":\"1234\",\"fom\":null,\"tom\":null}]}")));
    }

}
