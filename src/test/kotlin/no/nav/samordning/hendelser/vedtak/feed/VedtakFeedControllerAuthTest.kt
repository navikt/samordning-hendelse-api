package no.nav.samordning.hendelser.vedtak.feed

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.common.consumer.TpConfigConsumer
import no.nav.samordning.hendelser.common.security.support.SCOPE_SAMORDNING
import no.nav.samordning.hendelser.config.IntegrationTest
import no.nav.samordning.hendelser.vedtak.controller.VedtakFeedController.Companion.VEDTAK_HENDELSER_PATH
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.MOVED_PERMANENTLY
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Tests authentication/authorization (maskinportenValidatorTokenGenerator) of the feed controller API.
 */
@IntegrationTest
internal class VedtakFeedControllerAuthTest {

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var tpConfigConsumer: TpConfigConsumer

    @Test
    fun `Valid request is ok`() {
        mockMvc.get(VEDTAK_HENDELSER_PATH) {
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
        every { tpConfigConsumer.validateOrganisation(any(), any()) } returns false

        mockMvc.get(VEDTAK_HENDELSER_PATH) {
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
        mockMvc.get(VEDTAK_HENDELSER_PATH) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken("ILLEGAL_SCOPE", "889640782").serialize())
            }
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `Old enpoint is moved permanently`() {
        mockMvc.get(OLD_ENDPOINT) {
            param(TPNR_PARAM_NAME, TPNR_2)
        }.andExpect {
            status { isEqualTo(MOVED_PERMANENTLY.value()) }
        }

    }

    @Test
    fun `Missing token is unauthorized`() {
        mockMvc.get(VEDTAK_HENDELSER_PATH) {
            param(TPNR_PARAM_NAME, TPNR_2)
        }.andExpect {
            status { isUnauthorized() }
        }

    }

    @Test
    fun `Missing parameter is bad request`() {
        mockMvc.get(VEDTAK_HENDELSER_PATH) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    companion object {
        private const val OLD_ENDPOINT = "/hendelser"
        private const val TPNR_PARAM_NAME = "tpnr"
        private const val TPNR_1 = "1000"
        private const val TPNR_2 = "4000"
    }
}
