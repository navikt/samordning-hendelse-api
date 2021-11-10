package no.nav.samordning.hendelser.feed

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.feed.FeedController.Companion.SCOPE
import no.nav.samordning.hendelser.security.TpnrValidator
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.hasToString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMaskinportenValidator
internal class FeedControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var tpnrValidator: TpnrValidator

    @Autowired
    private lateinit var tokenGenerator: MaskinportenValidatorTokenGenerator

    @BeforeEach
    fun setup() {
        every { tpnrValidator.invoke(ORGNO, any()) } returns true
    }

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service() {
        mockMvc.get(GOOD_URL) {
            headers { setBearerAuth(token()) }
        }
            .andDo { print() }
            .andExpect {
                status().isOk
                content().contentType(MediaType.APPLICATION_JSON_VALUE)
            }
    }

    @Test
    @Throws(Exception::class)
    fun service_shouldnt_accept_too_large_requests() {
        mockMvc.get("/hendelser") {
            headers { setBearerAuth(token()) }
            param("tpnr", "1000")
            param("antall", "10001")
        }
            .andDo { print() }
            .andExpect {
                status().is4xxClientError
            }
    }

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service_with_first_record() {
        mockMvc.get("/hendelser") {
            headers { setBearerAuth(token()) }
            param("tpnr", "1000")
            param("antall", "1")
        }.andDo { print() }
            .andExpect {
                jsonPath("$.hendelser[0].identifikator", hasToString<Any>("01016600000"))
            }
    }

    @Test
    @Throws(Exception::class)
    fun greeting_should_return_message_from_service_with_size_check() {
        every { tpnrValidator(eq("4444444444"), any()) } returns true
        mockMvc.get("/hendelser") {
            headers {
                setBearerAuth(token(orgno = "4444444444"))
                param("tpnr", "4000")
                param("side", "0")
                param("antall", "5")
            }
        }.andExpect {
            status { isOk() }
            jsonPath("$.hendelser", hasSize<Any>(3))
        }
    }

    @Test
    @Throws(Exception::class)
    fun bad_parameters_return_400() {
        mockMvc.get("/hendelser") {
            headers {
                setBearerAuth(token(orgno = "4444444444"))
                param("tpnr", "4000")
                param("side", "-1")
            }
        }.andDo {
            print()
        }.andExpect {
            status().isBadRequest
        }
    }

    @Test
    @Throws(Exception::class)
    fun delete_method_is_not_allowed() {
        mockMvc.delete(GOOD_URL) {
            headers {
                setBearerAuth(token())
            }
        }.andExpect { status().isMethodNotAllowed }
    }

    @Test
    @Throws(Exception::class)
    fun patch_method_is_not_allowed() {
        mockMvc.patch(GOOD_URL) {
            headers {
                setBearerAuth(token())
            }
        }.andExpect {
            status().isMethodNotAllowed
        }
    }

    @Test
    @Throws(Exception::class)
    fun post_method_is_not_allowed() {
        mockMvc.put(GOOD_URL) {
            headers {
                setBearerAuth(token())
            }
        }.andExpect {
            status().isMethodNotAllowed
        }
    }

    @Test
    @Throws(Exception::class)
    fun put_method_is_not_allowed() {
        mockMvc.put(GOOD_URL) {
            headers {
                setBearerAuth(token())
            }
        }.andExpect {
            status().isMethodNotAllowed
        }
    }

    private fun token(scope: String = SCOPE, orgno: String = ORGNO) =
        tokenGenerator.generateToken(scope, orgno).serialize()

    companion object {
        private const val GOOD_URL = "/hendelser?tpnr=1000"
        private const val ORGNO = "0000000000"
    }
}
