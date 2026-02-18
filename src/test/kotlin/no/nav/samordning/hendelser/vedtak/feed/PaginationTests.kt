package no.nav.samordning.hendelser.vedtak.feed
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.common.security.support.SCOPE_SAMORDNING
import no.nav.samordning.hendelser.config.IntegrationTest

import org.hamcrest.core.IsNull
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@IntegrationTest
internal class PaginationTests {

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Test
    fun iterate_feed_with_next_page_url() {
        val nextUrl = JSONObject(
            mockMvc.get("/hendelser/vedtak?tpnr=4000&antall=2") {
                headers {
                    setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING,
                        PERMITTED_ORG_NO
                    ).serialize())
                }
            }.andReturn().response.contentAsString
        ).getString("nextUrl")

        assertEquals("$nextBaseUrl/hendelser/vedtak?tpnr=4000&sekvensnummer=1&antall=2&side=1", nextUrl)

        mockMvc.get(nextUrl)
            .andExpect {
                MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue())
            }
    }

    @Test
    fun iterate_feed_with_next_url_from_sekvensnummer() {
        val nextUrl = JSONObject(
                mockMvc.get("/hendelser/vedtak?tpnr=4000&sekvensnummer=2&antall=1") {
                    headers {
                        setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING,
                            PERMITTED_ORG_NO
                        ).serialize())
                    }
                }.andExpect {
                    MockMvcResultMatchers.jsonPath("$.sisteLesteSekvensnummer").value(2)
                }.andReturn().response.contentAsString
        ).getString("nextUrl")

        assertEquals("$nextBaseUrl/hendelser/vedtak?tpnr=4000&sekvensnummer=2&antall=1&side=1", nextUrl)

        mockMvc.get(nextUrl)
                .andExpect { MockMvcResultMatchers.jsonPath("$.sisteLesteSekvensnummer").value(3) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.nextUrl").value(IsNull.nullValue()) }
    }

    companion object {
        private const val PERMITTED_ORG_NO = "889640782"
    }
}
