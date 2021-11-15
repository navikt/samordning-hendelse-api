package no.nav.samordning.hendelser.feed

import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.feed.FeedController.Companion.SCOPE
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMaskinportenValidator
internal class PaginationTests {

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenGenerator: MaskinportenValidatorTokenGenerator

    @Test
    @Throws(Exception::class)
    fun iterate_feed_with_next_page_url() {
        val nextUrl = JSONObject(
            mockMvc.get("/hendelser") {
                headers { setBearerAuth(token()) }
                param("tpnr", "4000")
                param("antall", "2")
            }.andDo {
                print()
            }.andReturn().response.contentAsString
        ).getString("nextUrl")

                assertEquals("$nextBaseUrl/hendelser?tpnr=4000&sekvensnummer=1&antall=2&side=1", nextUrl)

                mockMvc.get(nextUrl) {
                    headers { setBearerAuth(token()) }
                }.andExpect {
                    jsonPath("$.nextUrl", nullValue())
            }
    }

    @Test
    @Throws(Exception::class)
    fun iterate_feed_with_next_url_from_sekvensnummer() {
        val nextUrl = JSONObject(
            mockMvc.get("/hendelser") {
                headers { setBearerAuth(token()) }
                param("tpnr", "4000")
                param("sekvensnummer", "2")
                param("antall", "1")
            }.andExpect {
                jsonPath("$.sisteLesteSekvensnummer", equalTo(2))
            }.andDo {
                print()
            }.andReturn().response.contentAsString
        ).getString("nextUrl")

        assertEquals("$nextBaseUrl/hendelser?tpnr=4000&sekvensnummer=2&antall=1&side=1", nextUrl)

        mockMvc.get(nextUrl) {
            headers { setBearerAuth(token()) }
        }.andExpect {
            jsonPath("$.sisteLesteSekvensnummer", equalTo(3))
            jsonPath("$.nextUrl", nullValue())
        }
    }

    private fun token() =
        tokenGenerator.generateToken(SCOPE, "4444444444").serialize()
}
