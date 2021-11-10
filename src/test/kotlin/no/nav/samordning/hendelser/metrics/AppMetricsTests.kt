package no.nav.samordning.hendelser.metrics

import io.micrometer.core.instrument.Counter
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.feed.FeedController.Companion.SCOPE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMaskinportenValidator
internal class AppMetricsTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var hendelserLestCounterMap: Map<String, Counter>

    @Test
    @Throws(Exception::class)
    fun hendelser_lest_metrics_are_incremented() {
        mockMvc.get("/hendelser") {
            headers { setBearerAuth(token()) }
            param("tpnr", "1000")
        }.andExpect {
            status { isOk() }
        }

        val count = hendelserLestCounterMap["1000"]?.count()
        assertNotNull(count)

        mockMvc.get("/hendelser") {
            headers { setBearerAuth(token()) }
            param("tpnr", "1000")
        }.andExpect {
            status { isOk() }
        }

        assertEquals(count!! + 1, hendelserLestCounterMap["1000"]?.count())
    }

    private fun token() =
        tokenGenerator.generateToken(SCOPE, "0000000000").serialize()
}
