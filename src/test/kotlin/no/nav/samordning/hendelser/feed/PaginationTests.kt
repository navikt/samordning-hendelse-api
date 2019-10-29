package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.TestAuthHelper.token
import org.hamcrest.core.IsNull
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
internal class PaginationTests {

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun iterate_feed_with_next_page_url() {
        val nextUrl = JSONObject(
                mockMvc.perform(get("/hendelser?tpnr=4000&antall=2")
                        .header("Authorization", token("4444444444", true)))
                        .andDo(print()).andReturn().response.contentAsString)
                .getString("nextUrl")

        assertEquals("$nextBaseUrl/hendelser?tpnr=4000&sekvensnummer=1&antall=2&side=1", nextUrl)

        mockMvc.perform(get(nextUrl)
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun iterate_feed_with_next_url_from_sekvensnummer() {
        val nextUrl = JSONObject(
                mockMvc.perform(get("/hendelser?tpnr=4000&sekvensnummer=5&antall=1")
                        .header("Authorization", token("4444444444", true)))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.sisteSekvensnummer").value(6))
                        .andDo(print()).andReturn().response.contentAsString)
                .getString("nextUrl")

        assertEquals("$nextBaseUrl/hendelser?tpnr=4000&sekvensnummer=5&antall=1&side=1", nextUrl)

        mockMvc.perform(get(nextUrl)
                .header("Authorization", token("4444444444", true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sisteSekvensnummer").value(6))
    }
}
