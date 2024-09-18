package no.nav.samordning.hendelser.feed

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.security.support.SCOPE_SAMORDNING
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMaskinportenValidator
//@EnableAutoConfiguration(exclude=[DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureMockMvc
@Disabled
internal class FeedControllerTest {

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var mockMvc: MockMvc

    @ParameterizedTest(name = "Valid requests returns ok with content")
    @ValueSource(strings = [URL_VEDTAK, URL_VEDTAK_YTELSE, URL_TP_YTELSER])
    fun `valid requests returns ok with content`(url: String) {
        mockMvc.get(url) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            }
        }.andDo { print() }
        .andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @ParameterizedTest(name = "Service should not accept too large requests")
    @ValueSource(strings = [URL_VEDTAK, URL_VEDTAK_YTELSE])
    fun `service shouldnt accept too large requests`(url: String) {
        mockMvc.get(url) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            }
            param("antall", "10001")
        }.andDo {
            print()
        }.andExpect {
            status { is4xxClientError() }
        }
    }

    @ParameterizedTest(name = "Should return message from service with first record")
    @CsvSource("$URL_VEDTAK, 01016600000", "$URL_VEDTAK_YTELSE, 01019000000")
    fun `should return message from service with first record`(url: String, expected: String) {
        mockMvc.get("$url&antall=1") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            }
        }.andExpect {
            jsonPath("$.hendelser[0].identifikator") { value(expected) }
        }.andDo {
            print()
        }
    }

    @ParameterizedTest(name = "Should return message from service with size check")
    @CsvSource("/hendelser?tpnr=4000&side=0&antall=5, 3", "$URL_VEDTAK_YTELSE&antall=5, 1")
    fun `should return message from service with size check`(url: String, expected: String) {
        mockMvc.get(url) {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            }
        }.andExpect {
            jsonPath("$.hendelser.size()") { expected.toInt() }
        }
    }

    @Test
    fun bad_parameters_return_400() {
        mockMvc.get("/hendelser?tpnr=4000&side=-1") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").parsedString)
            }
        }.andDo { print() }
        .andExpect {
            status { isUnauthorized() }
        }
    }

    companion object {

        private const val URL_VEDTAK = "/hendelser?tpnr=1000"
        private const val URL_VEDTAK_YTELSE = "/hendelser/vedtak/ytelse?tpnr=6000&ytelse=OMS"
        private const val URL_TP_YTELSER = "/hendelser/tp/ytelser?tpnr=1000"
    }
}
