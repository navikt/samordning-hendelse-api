package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.TestDataHelper;
import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaginationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataHelper testData;

    @Value("${FEED_MAX_ANTALL}")
    private String maxAntall;

    @Test
    public void iterate_feed_with_next_page_url() throws Exception {
        String nextUrl = new JSONObject(
            mockMvc.perform(get("/hendelser").with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString())
            .getString("next_url");
        assertEquals("http://localhost/hendelser?side=1&antall=5", nextUrl);

        String lastUrl = new JSONObject(
            mockMvc.perform(get(nextUrl).with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString())
            .getString("next_url");
        assertEquals("null", lastUrl);
    }

    @Test
    public void first_page_links_to_second_page_with_remaining_items() throws Exception {
        List<Hendelse> firstPage = testData.mapJsonToHendelser(
            mockMvc.perform(get("/hendelser?side=0&antall=4")
                .with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString());

        List<Hendelse> secondPage = testData.mapJsonToHendelser(
            mockMvc.perform(get("/hendelser?side=1&antall=4")
                .with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString());

        List<Hendelse> thirdPage = testData.mapJsonToHendelser(
            mockMvc.perform(get("/hendelser?side=2&antall=4")
                .with(user("srvTest")))
                .andDo(print()).andReturn().getResponse().getContentAsString());

        assertTrue(firstPage.stream().allMatch(hendelse -> testData.hendelseIdList("0", "1", "2", "3")
            .contains(hendelse.getIdentifikator())));
        assertTrue(secondPage.stream().allMatch(hendelse -> testData.hendelseIdList("4", "5", "6", "7")
            .contains(hendelse.getIdentifikator())));
        assertTrue(thirdPage.stream().allMatch(hendelse -> testData.hendelseIdList("8", "9")
            .contains(hendelse.getIdentifikator())));
    }
}
