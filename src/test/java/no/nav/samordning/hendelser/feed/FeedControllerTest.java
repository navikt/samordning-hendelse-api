package no.nav.samordning.hendelser.feed;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.security.NoSuchAlgorithmException;

import static no.nav.samordning.hendelser.TestAuthHelper.token;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeedControllerTest {

    private static final String GOOD_URL = "/hendelser?tpnr=1000";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greeting_should_return_message_from_service() throws Exception {
        mockMvc.perform(get(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    void service_shouldnt_accept_too_large_requests() throws Exception {
        mockMvc.perform(get("/hendelser")
                .header("Authorization", prepareToken())
                .param("antall", "10001"))
                .andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    void greeting_should_return_message_from_service_with_first_record() throws Exception {
        mockMvc.perform(get("/hendelser?tpnr=1000&antall=1")
                .header("Authorization", prepareToken()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator", hasToString("01016600000")))
                .andDo(print());
    }

    @Test
    void greeting_should_return_message_from_service_with_size_check() throws Exception {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=0&antall=5")
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize(3)));
    }

    @Test
    void bad_parameters_return_400() throws Exception {
        mockMvc.perform(get("/hendelser?tpnr=4000&side=-1")
                .header("Authorization", token("4444444444", true)))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    void delete_method_is_not_allowed() throws Exception {
        mockMvc.perform(delete(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void patch_method_is_not_allowed() throws Exception {
        mockMvc.perform(patch(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void post_method_is_not_allowed() throws Exception {
        mockMvc.perform(post(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void put_method_is_not_allowed() throws Exception {
        mockMvc.perform(put(GOOD_URL)
                .header("Authorization", prepareToken()))
                .andExpect(status().isMethodNotAllowed());
    }

    @NotNull
    private static String prepareToken() throws NoSuchAlgorithmException {
        return token("0000000000", true);
    }
}
