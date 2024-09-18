package no.nav.samordning.hendelser.feed

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests authentication/authorization (maskinportenValidatorTokenGenerator) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMaskinportenValidator
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@Disabled
internal class FeedControllerAuthTest {

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Valid request is ok`() {
        val headers = HttpHeaders()
            headers.add("Authorization", "Bearer " + maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            mockMvc.perform(get(URL_VEDTAK)
                .headers(headers)
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isOk)
    }

    @Test
    fun `Request missing validation is forbidden`() {
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer " + maskinportenValidatorTokenGenerator.generateToken("nav:pensjon/v1/tptp", "889640782").parsedString)
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isForbidden)
    }

    @Test
    fun `Missing token is unauthorized`() {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Missing parameter is bad request`() {
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer " + maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
        mockMvc.perform(get(ENDPOINT)
                .headers(headers))
                .andExpect(status().isBadRequest)
    }

    companion object {
        private const val URL_VEDTAK = "/hendelser?tpnr=1000"
        private const val SCOPE_SAMORDNING = "nav:pensjon/v1/samordning"
        private const val ENDPOINT = "/hendelser"
        private const val TPNR_PARAM_NAME = "tpnr"
        private const val TPNR_1 = "1000"
        private const val TPNR_2 = "4000"
    }
}
