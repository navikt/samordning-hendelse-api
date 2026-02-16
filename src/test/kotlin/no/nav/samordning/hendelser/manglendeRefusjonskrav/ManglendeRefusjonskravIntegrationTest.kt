package no.nav.samordning.hendelser.manglendeRefusjonskrav

import io.mockk.mockk
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.pensjonsamhandling.maskinporten.validation.test.AutoConfigureMaskinportenValidator
import no.nav.pensjonsamhandling.maskinporten.validation.test.MaskinportenValidatorTokenGenerator
import no.nav.samordning.hendelser.manglendeRefusjonskrav.kafka.ManglendeRefusjonskravKafkaHendelse
import no.nav.samordning.hendelser.manglendeRefusjonskrav.kafka.ManglendeRefusjonskravListener
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskravRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMaskinportenValidator
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureMockMvc
class ManglendeRefusjonskravIntegrationTest {

    @Autowired
    private lateinit var manglendeRefusjonskravRepository: ManglendeRefusjonskravRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var maskinportenValidatorTokenGenerator: MaskinportenValidatorTokenGenerator

    private lateinit var listener: ManglendeRefusjonskravListener
    private lateinit var acknowledgment: Acknowledgment

    @BeforeEach
    fun setup() {
        listener = ManglendeRefusjonskravListener(manglendeRefusjonskravRepository, objectMapper)
        acknowledgment = mockk(relaxed = true)
        manglendeRefusjonskravRepository.deleteAll()
    }

    @AfterEach
    fun teardown() {
        manglendeRefusjonskravRepository.deleteAll()
    }

    @Test
    fun `skal lagre innkommende melding og hente den via REST API`() {
        val tpnr = "0129"
        val fnr = "12345678901"
        val samId = "98765432109"
        val svarfrist = LocalDate.of(2025, 12, 31)

        val hendelse = ManglendeRefusjonskravKafkaHendelse(
            tpNr = tpnr,
            fnr = fnr,
            samId = samId,
            svarfrist = svarfrist
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        // 1. Listener mottar og lagrer melding
        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        // 2. Verifiser at data er lagret i databasen
        val saved = manglendeRefusjonskravRepository.findAll()
        assertEquals(1, saved.size)

        val lagretData = saved[0]
        assertEquals(fnr, lagretData.fnr)
        assertEquals(tpnr, lagretData.tpnr)
        assertEquals(samId, lagretData.samId)
        assertEquals(svarfrist, lagretData.svarfrist)
        assertEquals(1L, lagretData.sekvensnummer)

        // 3. Hent data via REST API
        mockMvc.get("/hendelser/manglendeRefusjonskrav?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn().response.contentAsString.let { response ->
            assertTrue(response.contains(fnr), "Respons skal inneholde fnr")
            assertTrue(response.contains(samId), "Respons skal inneholde samId")
            assertTrue(response.contains("0129"), "Respons skal inneholde tpnr")
        }
    }

    @Test
    fun `skal lagre flere meldinger med inkrementert sekvensnummer`() {
        val tpnr = "0129"
        val svarfrist = LocalDate.of(2025, 12, 31)

        // Lagre 3 meldinger
        repeat(3) { i ->
            val hendelse = ManglendeRefusjonskravKafkaHendelse(
                tpNr = tpnr,
                fnr = "1234567890${i}",
                samId = "9876543210${i}",
                svarfrist = svarfrist
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)

            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        // Verifiser at alle 3 er lagret med riktig sekvensnummer
        val saved = manglendeRefusjonskravRepository.findAll()
        assertEquals(3, saved.size)

        assertEquals(1L, saved[0].sekvensnummer)
        assertEquals(2L, saved[1].sekvensnummer)
        assertEquals(3L, saved[2].sekvensnummer)

        // Hent fra REST API med sekvensnummer=2
        mockMvc.get("/hendelser/manglendeRefusjonskrav?tpnr=$tpnr&sekvensnummer=2") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString.let { response ->
            assertTrue(response.contains("12345678901"), "Skal hente record med sekvensnummer 2")
        }
    }

    @Test
    fun `skal lagre meldinger fra ulike tpnr uavhengig`() {
        val tpnr1 = "0129"
        val tpnr2 = "0130"
        val svarfrist = LocalDate.of(2025, 12, 31)

        // Lagre 2 meldinger for tpnr1
        repeat(2) { i ->
            val hendelse = ManglendeRefusjonskravKafkaHendelse(
                tpNr = tpnr1,
                fnr = "1111111111${i}",
                samId = "9999999999${i}",
                svarfrist = svarfrist
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        // Lagre 3 meldinger for tpnr2
        repeat(3) { i ->
            val hendelse = ManglendeRefusjonskravKafkaHendelse(
                tpNr = tpnr2,
                fnr = "2222222222${i}",
                samId = "8888888888${i}",
                svarfrist = svarfrist
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        // Verifiser at tpnr1 har sekvensnummer 1-2 og tpnr2 har sekvensnummer 1-3
        val tpnr1Records = manglendeRefusjonskravRepository.findAll().filter { it.tpnr == tpnr1 }
        val tpnr2Records = manglendeRefusjonskravRepository.findAll().filter { it.tpnr == tpnr2 }

        assertEquals(2, tpnr1Records.size)
        assertEquals(3, tpnr2Records.size)

        assertEquals(listOf(1L, 2L), tpnr1Records.map { it.sekvensnummer }.sorted())
        assertEquals(listOf(1L, 2L, 3L), tpnr2Records.map { it.sekvensnummer }.sorted())

        // Verifiser at REST API returnerer korrekte data for hver tpnr
        mockMvc.get("/hendelser/manglendeRefusjonskrav?tpnr=$tpnr1") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andReturn().response.contentAsString.let { response ->
            assertTrue(response.contains("1111111111"), "Skal inneholde fnr fra tpnr1")
            assertTrue(!response.contains("2222222222"), "Skal ikke inneholde fnr fra tpnr2")
        }
    }

    @Test
    fun `skal returnere riktig antall hendelser fra REST API`() {
        val tpnr = "0888"
        val svarfrist = LocalDate.of(2025, 12, 31)

        // Lagre 15 meldinger
        repeat(15) { i ->
            val hendelse = ManglendeRefusjonskravKafkaHendelse(
                tpNr = tpnr,
                fnr = "1234567890${i.toString().padStart(2, '0')}",
                samId = "9876543210${i.toString().padStart(2, '0')}",
                svarfrist = svarfrist
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        // Hent med antall=10
        mockMvc.get("/hendelser/manglendeRefusjonskrav?tpnr=$tpnr&antall=10") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andReturn().response.contentAsString.let { response ->
            val countMatches = response.split("\"fnr\"").size - 1
            assertTrue(countMatches >= 10, "Skal returnere minst 10 hendelser når antall=10, fikk $countMatches")
        }
    }

    @Test
    fun `skal håndtere ugyldig JSON og ikke lagre`() {
        val invalidJson = "{ invalid json }"
        val consumerRecord = mockConsumerRecord(invalidJson)

        listener.listener(invalidJson, consumerRecord, acknowledgment)

        val saved = manglendeRefusjonskravRepository.findAll()
        assertEquals(0, saved.size)
    }

    @Test
    fun `skal hente siste sekvensnummer fra API respons`() {
        val tpnr = "0877"
        val svarfrist = LocalDate.of(2025, 12, 31)

        repeat(5) { i ->
            val hendelse = ManglendeRefusjonskravKafkaHendelse(
                tpNr = tpnr,
                fnr = "1234567890${i}",
                samId = "9876543210${i}",
                svarfrist = svarfrist
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        mockMvc.get("/hendelser/manglendeRefusjonskrav?tpnr=$tpnr") {
            headers {
                setBearerAuth(maskinportenValidatorTokenGenerator.generateToken(SCOPE_SAMORDNING, PERMITTED_ORG_NO).serialize())
            }
        }.andReturn().response.contentAsString.let { response ->
            assertTrue(response.contains("\"sisteSekvensnummer\":5"), "Skal ha sisteSekvensnummer=5")
        }
    }

    private fun mockConsumerRecord(value: String): ConsumerRecord<String, String> {
        return ConsumerRecord(
            "MANGLENDE_REFUSJONSKRAV",
            0,
            1L,
            UUID.randomUUID().toString(),
            value
        )
    }

    companion object {
        private const val SCOPE_SAMORDNING = "nav:pensjon/v1/samordning"
        private const val PERMITTED_ORG_NO = "889640782"
    }
}
