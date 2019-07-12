package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.TestTokenHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class PaginationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void iterate_feed_with_next_page_url() throws Exception {
        String nextUrl = new JSONObject(
            mockMvc.perform(get("/hendelser?tpnr=4000&antall=2")
                .header("Authorization", TestTokenHelper.getValidAccessToken()))
                .andDo(print()).andReturn().getResponse().getContentAsString())
            .getString("next_url");

        assertEquals("http://localhost/hendelser?tpnr=4000&side=1&antall=2", nextUrl);

        mockMvc.perform(get(nextUrl)
            .header("Authorization", TestTokenHelper.getValidAccessToken()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.next_url", isEmptyOrNullString()));
    }
}
