package no.nav.samordning.hendelser.feed

import com.ninjasquad.springmockk.MockkBean
import io.mockk.MockKMatcherScope
import io.mockk.every
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.feed.FeedController.Companion.SCOPE
import no.nav.samordning.hendelser.security.TpnrValidator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import javax.servlet.http.HttpServletRequest

/**
 * Tests authentication/authorization (token validation) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMaskinportenValidator
internal class FeedControllerAuthTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenGenerator: MaskinportenValidatorTokenGenerator

    @MockkBean
    private lateinit var tpnrValidator: TpnrValidator

    @Test
    @Throws(Exception::class)
    fun valid_token_issuer_is_authorized() {
        every { tpnrValidator(eq(ORG_NUMBER_1), withTpnr(TPNR_1)) } returns true
        mockMvc.get(ENDPOINT) {
            withTokenFor(ORG_NUMBER_1)
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @Throws(Exception::class)
    fun valid_orgno_and_tpnr_is_authorized() {
        every { tpnrValidator(eq(ORG_NUMBER_2), withTpnr(TPNR_2)) } returns true
        mockMvc.get(ENDPOINT) {
            withTokenFor(ORG_NUMBER_2)
            param(TPNR_PARAM_NAME, TPNR_2)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    @Throws(Exception::class)
    fun invalid_orgno_is_forbidden() {
        every { tpnrValidator(eq("1111111111"), any()) } returns false
        mockMvc.get(ENDPOINT) {
            withTokenFor("1111111111")
            param(TPNR_PARAM_NAME, TPNR_1)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @Throws(Exception::class)
    fun invalid_tpnr_is_forbidden() {
        every { tpnrValidator(any(), withTpnr("1235")) } returns false
        mockMvc.get(ENDPOINT) {
            withTokenFor(ORG_NUMBER_1)
            param(TPNR_PARAM_NAME, "1235")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @Throws(Exception::class)
    fun invalid_orgno_and_tpnr_is_forbidden() {
        every { tpnrValidator(any(), any()) } returns false
        mockMvc.get(ENDPOINT) {
            withTokenFor("9999999999")
            param(TPNR_PARAM_NAME, "2000")
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun no_token_is_unauthorized() {
        mockMvc.get(ENDPOINT) {
            param(TPNR_PARAM_NAME, TPNR_2)
        }.andExpect {
            status {
                isUnauthorized()
            }
        }
    }

    private fun MockHttpServletRequestDsl.withTokenFor(orgno: String) =
        headers {
            setBearerAuth(
                tokenGenerator.generateToken(SCOPE, orgno).serialize()
            )
        }

    private fun MockKMatcherScope.withTpnr(tpnr: String) = match<HttpServletRequest> {
        it.getParameter("tpnr").substringBefore('?') == tpnr
    }

    companion object {
        private const val ENDPOINT = "/hendelser"
        private const val TPNR_PARAM_NAME = "tpnr"
        private const val ORG_NUMBER_1 = "0000000000"
        private const val ORG_NUMBER_2 = "4444444444"
        private const val TPNR_1 = "1000"
        private const val TPNR_2 = "4000"
    }
}
