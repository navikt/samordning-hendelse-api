package no.nav.samordning.hendelser.feed

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Tests authentication/authorization (maskinportenValidatorTokenGenerator) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMaskinportenValidator
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
internal class FeedControllerAuthTest {

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Valid request is ok`() {
        mockMvc.get(ENDPOINT) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `Request failing validation is forbidden`() {
        mockMvc.get(ENDPOINT) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640777").serialize())
            }
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `Request illegal scope is forbidden`() {
        mockMvc.get(ENDPOINT) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken("ILLEGAL_SCOPE", "889640782").serialize())
            }
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `Missing token is unauthorized`() {
        mockMvc.get(ENDPOINT) {
            param(TPNR_PARAM_NAME, TPNR_2)
        }.andExpect {
            status { isUnauthorized() }
        }

    }

    @Test
    fun `Missing parameter is bad request`() {
        mockMvc.get(ENDPOINT) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
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
