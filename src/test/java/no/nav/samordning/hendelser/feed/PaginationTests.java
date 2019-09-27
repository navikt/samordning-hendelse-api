package no.nav.samordning.hendelser.feed;

import org.hamcrest.core.IsNull;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static no.nav.samordning.hendelser.TestAuthHelper.token;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class PaginationTests {

    @Value("${NEXT_BASE_URL}")
    private String nextBaseUrl;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void iterate_feed_with_next_page_url() throws Exception {
        String nextUrl = new JSONObject(
                mockMvc.perform(get("/hendelser?tpnr=4000&antall=2")
                        .header("Authorization", token("4444444444", true)))
                        .andDo(print()).andReturn().getResponse().getContentAsString())
                .getString("nextUrl");

        assertEquals(nextBaseUrl + "/hendelser?tpnr=4000&sekvensnummer=1&antall=2&side=1", nextUrl);

        mockMvc.perform(get(nextUrl)
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue()));
    }

    @Test
    void iterate_feed_with_next_url_from_sekvensnummer() throws Exception {
        String nextUrl = new JSONObject(
                mockMvc.perform(get("/hendelser?tpnr=4000&sekvensnummer=5&antall=1")
                        .header("Authorization", token("4444444444", true)))
                        .andDo(print()).andReturn().getResponse().getContentAsString())
                .getString("nextUrl");

        assertEquals(nextBaseUrl + "/hendelser?tpnr=4000&sekvensnummer=5&antall=1&side=1", nextUrl);

        mockMvc.perform(get(nextUrl)
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue()));
    }
}
