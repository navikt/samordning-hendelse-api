package no.nav.samordning.hendelser.person.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.domain.PersonResponse
import no.nav.samordning.hendelser.person.service.PersonService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMaskinportenValidator
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureMockMvc
internal class PersonFeedControllerTest {

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var personService: PersonService

    // ============ Grunnleggende OK-tester ============

    @Test
    fun `gyldig request skal returnere 200 OK`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andDo { print() }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
    }

    @Test
    fun `gyldig request skal returnere JSON respons med hendelser`() {
        val tpnr = "2000"
        val personResponse = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = "GIFT",
            sivilstandDato = LocalDate.of(2020, 1, 1),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
            jsonPath("$.hendelser[0].fnr") { value("01016600000") }
            jsonPath("$.hendelser[0].meldingskode") { value("SIVILSTAND") }
            jsonPath("$.hendelser[0].sivilstand") { value("GIFT") }
        }
    }

    // ============ Parametervalidering ============

    @ParameterizedTest(name = "Ugyldig tpnr format {0} skal returnere 400")
    @ValueSource(strings = ["", "12345", "abc", "1000.0"])
    fun `ugyldig tpnr format skal returnere 400`(invalidTpnr: String) {
        mockMvc.get("/hendelser/personer?tpnr=$invalidTpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `manglende tpnr parameter skal returnere 400`() {
        mockMvc.get("/hendelser/personer") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @ParameterizedTest(name = "Negative side verdi {0} skal returnere 400")
    @ValueSource(strings = ["-1", "-100"])
    fun `negative side verdi skal returnere 400`(negativeSide: String) {
        mockMvc.get("/hendelser/personer?tpnr=1000&side=$negativeSide") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `side verdi 0 skal aksepteres`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&side=0") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @ParameterizedTest(name = "Negative antall verdi {0} skal returnere 400")
    @ValueSource(strings = ["-1", "-10000"])
    fun `negative antall verdi skal returnere 400`(negativeAntall: String) {
        mockMvc.get("/hendelser/personer?tpnr=1000&antall=$negativeAntall") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `antall verdi 0 skal aksepteres`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 0L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 0L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&antall=0") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `for stor antall verdi over 10000 skal returnere 400`() {
        mockMvc.get("/hendelser/personer?tpnr=1000&antall=10001") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `antall verdi 10000 skal aksepteres`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&antall=10000") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @ParameterizedTest(name = "Negative eller null sekvensnummer verdi {0} skal returnere 400")
    @ValueSource(strings = ["0", "-1", "-999"])
    fun `negative eller null sekvensnummer verdi skal returnere 400`(invalidSekvens: String) {
        mockMvc.get("/hendelser/personer?tpnr=1000&sekvensnummer=$invalidSekvens") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `sekvensnummer verdi 1 skal aksepteres`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&sekvensnummer=1") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    // ============ Standardverdier ============

    @Test
    fun `default verdier side skal være 0`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `default verdier antall skal være 10000`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `default verdier sekvensnummer skal være 1`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    // ============ Respons struktur ============

    @Test
    fun `respons skal inneholde hendelser liste`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser") { isArray() }
        }
    }

    @Test
    fun `respons skal inneholde sisteLesteSekvensNummer`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.sisteLesteSekvensnummer") { exists() }
        }
    }

    @Test
    fun `respons skal inneholde nyesteSekvensNummer`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 50L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.sisteSekvensnummer") { value(50) }
        }
    }

    @Test
    fun `tom hendelsesliste skal returnere next som null`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.nextUrl") { doesNotExist() }
        }
    }

    @Test
    fun `hendelsesliste med flere sider skal inneholde next link`() {
        val tpnr = "1000"
        val personResponse = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = "GIFT",
            sivilstandDato = LocalDate.of(2020, 1, 1),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 1000L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 5000L
        every { personService.getNumberOfPages(tpnr, 1L, 1000L) } returns 5L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&antall=1000&side=0") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.nextUrl") { exists() }
        }
    }

    // ============ Hendelsesdata ============

    @Test
    fun `hendelse skal inneholde alle felter`() {
        val tpnr = "1000"
        val personResponse = PersonResponse(
            sekvensnummer = 42L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = "01016600001",
            sivilstand = "SAMBOER",
            sivilstandDato = LocalDate.of(2021, 5, 15),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 42L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser[0].sekvensnummer") { value(42) }
            jsonPath("$.hendelser[0].tpnr") { value(tpnr) }
            jsonPath("$.hendelser[0].fnr") { value("01016600000") }
            jsonPath("$.hendelser[0].fnrGammelt") { value("01016600001") }
            jsonPath("$.hendelser[0].sivilstand") { value("SAMBOER") }
            jsonPath("$.hendelser[0].sivilstandDato") { value("2021-05-15") }
            jsonPath("$.hendelser[0].doedsdato") { doesNotExist() }
            jsonPath("$.hendelser[0].meldingskode") { value("SIVILSTAND") }
        }
    }

    @ParameterizedTest(name = "Meldingskode {0} skal mappes korrekt")
    @CsvSource(
        "SIVILSTAND",
        "FODSELSNUMMER",
        "ADRESSE",
        "DOEDSFALL"
    )
    fun `alle meldingskoder skal mappes korrekt`(meldingskode: String) {
        val tpnr = "1000"
        val personResponse = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = null,
            sivilstandDato = null,
            doedsdato = null,
            meldingskode = Meldingskode.valueOf(meldingskode)
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser[0].meldingskode") { value(meldingskode) }
        }
    }

    @Test
    fun `hendelse med doedsdato skal inneholde felt`() {
        val tpnr = "1000"
        val personResponse = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = null,
            sivilstandDato = null,
            doedsdato = LocalDate.of(2023, 12, 25),
            meldingskode = Meldingskode.DOEDSFALL
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser[0].doedsdato") { value("2023-12-25") }
        }
    }

    // ============ Paginering ============

    @Test
    fun `side parameter skal påvirke pagination`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 1L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&side=1") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `sekvensnummer parameter skal påvirke resultat`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 100L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 100L
        every { personService.getNumberOfPages(tpnr, 100L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&sekvensnummer=100") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `antall parameter skal påvirke resultatsett størrelse`() {
        val tpnr = "1000"
        val personResponse = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = "GIFT",
            sivilstandDato = LocalDate.of(2020, 1, 1),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 5L) } returns listOf(personResponse)
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 5L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr&antall=5") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `tom liste skal returnere tom hendelser array`() {
        val tpnr = "1000"
        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns emptyList()
        every { personService.latestSekvensnummer(tpnr) } returns 1L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser.size()") { value(0) }
        }
    }

    @Test
    fun `multiple hendelser skal returneres i liste`() {
        val tpnr = "1000"
        val person1 = PersonResponse(
            sekvensnummer = 1L,
            tpnr = tpnr,
            fnr = "01016600000",
            fnrGammelt = null,
            sivilstand = "GIFT",
            sivilstandDato = LocalDate.of(2020, 1, 1),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )
        val person2 = PersonResponse(
            sekvensnummer = 2L,
            tpnr = tpnr,
            fnr = "02016600000",
            fnrGammelt = null,
            sivilstand = "UGIFT",
            sivilstandDato = LocalDate.of(2021, 1, 1),
            doedsdato = null,
            meldingskode = Meldingskode.SIVILSTAND
        )

        every { personService.fetchSeqAndPersonEndringHendelser(tpnr, 1L, 0L, 10000L) } returns listOf(person1, person2)
        every { personService.latestSekvensnummer(tpnr) } returns 2L
        every { personService.getNumberOfPages(tpnr, 1L, 10000L) } returns 0L

        mockMvc.get("/hendelser/personer?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, "889640782").serialize())
            }
        }.andExpect {
            jsonPath("$.hendelser.size()") { value(2) }
            jsonPath("$.hendelser[0].fnr") { value("01016600000") }
            jsonPath("$.hendelser[1].fnr") { value("02016600000") }
        }
    }

    companion object {
        private const val SCOPE_SAMORDNING = "nav:pensjon/v1/samordning"
    }
}
