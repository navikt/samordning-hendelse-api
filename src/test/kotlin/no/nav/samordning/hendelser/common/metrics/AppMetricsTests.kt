package no.nav.samordning.hendelser.common.metrics

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.verify
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.common.security.support.SCOPE_SAMORDNING
import no.nav.samordning.hendelser.config.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@IntegrationTest
internal class AppMetricsTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @MockkSpyBean
    private lateinit var appMetrics: AppMetrics

    @Test
    fun hendelser_lest_metrics_are_incremented() {
        mockMvc.get("/hendelser?tpnr=1000") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andExpect{
            status {isOk() }
        }

        verify(exactly = 1) { appMetrics.incHendelserLest(eq("1000"), any()) }
    }

    companion object {
        private const val PERMITTED_ORG_NO = "889640782"
    }
}
