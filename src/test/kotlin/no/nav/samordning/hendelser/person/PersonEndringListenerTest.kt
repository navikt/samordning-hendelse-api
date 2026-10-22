package no.nav.samordning.hendelser.person

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.samordning.hendelser.common.consumer.OrdningDto
import no.nav.samordning.hendelser.common.consumer.TpConfigConsumer
import no.nav.samordning.hendelser.config.IntegrationTest
import no.nav.samordning.hendelser.person.domain.Adresse
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.kafka.PersonEndringKafkaHendelse
import no.nav.samordning.hendelser.person.kafka.PersonEndringListener
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import no.nav.samordning.hendelser.person.repository.PersonHendelseRepository
import no.nav.samordning.hendelser.person.sporingslogg.SporingsloggProducer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.SendResult
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@IntegrationTest
class PersonEndringListenerTest {

    @MockkBean(relaxed = true)
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockkBean(relaxed = true)
    private lateinit var tpconsumer: TpConfigConsumer

    @Autowired
    private lateinit var personEndringRepository: PersonEndringRepository

    @Autowired
    private lateinit var personHendelseRepository: PersonHendelseRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var sporingsloggProducer: SporingsloggProducer

    private lateinit var listener: PersonEndringListener
    private lateinit var acknowledgment: Acknowledgment
    //private val mockFuture = CompletableFuture.completedFuture(mockk<SendResult<String, String>>())


    @BeforeEach
    fun setup() {
        every { kafkaTemplate.send(any<String>(), any<String>()) } returns CompletableFuture.completedFuture(mockk<SendResult<String, String>>())
        //every { kafkaTemplate.send(any<String>(), any<String>()) } returns mockFuture
        coEvery { tpconsumer.hentOrdning (any()) } returns OrdningDto("Dummy", "1234", "90110223121", "8000999012", emptyList())

        sporingsloggProducer = SporingsloggProducer(kafkaTemplate, "sporingsloog-topic", tpconsumer)
        listener = PersonEndringListener(personEndringRepository, personHendelseRepository, sporingsloggProducer, objectMapper)

        acknowledgment = mockk(relaxed = true)
        personEndringRepository.deleteAll()
        personHendelseRepository.deleteAll()
    }

    @AfterEach
    fun teardown() {
        personEndringRepository.deleteAll()
        personHendelseRepository.deleteAll()
    }

    @Test
    fun `skal lagre personendring med adresse med enkelt tpnr`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "1234"
        val meldingskode = Meldingskode.ADRESSE

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = meldingskode,
            adresse = Adresse(
                adresselinje1 = "Adresselinje1",
                adresselinje2 = "Adresselinje2",
                adresselinje3 = "Adresselinje3",
                postnr = "1234",
                poststed = "Poststed",
                land = "Land"
            )
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        val saved = personEndringRepository.findAll()
        assertEquals(1, saved.size)
        assertEquals(fnr, saved[0].fnr)
        assertEquals(tpnr, saved[0].tpnr)
        assertEquals(meldingskode, saved[0].meldingskode)
        assertEquals(1L, saved[0].sekvensnummer)

        assertNotNull(saved[0].adresse)
        assertEquals("Adresselinje2", saved[0].adresse?.adresselinje2)

        verify(exactly = 1) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal lagre personendring med enkelt tpnr`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "1234"
        val meldingskode = Meldingskode.SIVILSTAND

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = meldingskode,
            sivilstand = "GIFT",
            sivilstandDato = LocalDate.now()
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        val saved = personEndringRepository.findAll()
        assertEquals(1, saved.size)
        assertEquals(fnr, saved[0].fnr)
        assertEquals(tpnr, saved[0].tpnr)
        assertEquals(meldingskode, saved[0].meldingskode)
        assertEquals(1L, saved[0].sekvensnummer)

        verify(exactly = 1) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify { acknowledgment.acknowledge() }

    }

    @Test
    fun `skal lagre personendring med flere tpnr`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnrList = listOf("1234", "5657", "8910")
        val meldingskode = Meldingskode.FODSELSNUMMER

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = tpnrList,
            fnr = fnr,
            oldFnr = "98765432101",
            meldingsKode = meldingskode
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        val saved = personEndringRepository.findAll()
        assertEquals(3, saved.size)
        tpnrList.forEach { tpnr ->
            assertTrue(saved.any { it.tpnr == tpnr })
        }

        verify(exactly = 3) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify { acknowledgment.acknowledge() }

    }

    @Test
    fun `skal sette sekvensnummer basert paa siste sekvensnummer for tpnr`() {
        val tpnr = "1234"
        val fnr = "12345678901"

        // Lagre to tidligere hendelser
        repeat(2) { _ ->
            val hendelse = PersonEndringKafkaHendelse(
                hendelseId = UUID.randomUUID().toString(),
                tpNr = listOf(tpnr),
                fnr = fnr,
                meldingsKode = Meldingskode.SIVILSTAND
            )
            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        // Lagre ny hendelse
        val hendelseId = UUID.randomUUID().toString()
        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = Meldingskode.DOEDSFALL,
            dodsdato = LocalDate.now()
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        val saved = personEndringRepository.findAll().last { it.tpnr == tpnr }
        assertNotNull(saved)
        assertEquals(3L, saved.sekvensnummer)
    }

    @Test
    fun `skal ignorere duplikat hendelse basert paa hendelseId og meldingskode`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "1234"
        val meldingskode = Meldingskode.SIVILSTAND

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = meldingskode
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        // Lagre første gang
        listener.listener(hendelseJson, consumerRecord, acknowledgment)
        assertEquals(1, personEndringRepository.findAll().size)

        // Lagre samme hendelse igjen
        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        val saved = personEndringRepository.findAll()
        assertEquals(1, saved.size)

        verify(exactly = 1) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify(exactly = 2) { acknowledgment.acknowledge() }

    }

    @Test
    fun `skal lagre personHendelse med hendelseId og meldingskode`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "1234"
        val meldingskode = Meldingskode.ADRESSE

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = meldingskode
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        listener.listener(hendelseJson, consumerRecord, acknowledgment)

        assertTrue(personHendelseRepository.existsByHendelseIdAndMeldingskode(hendelseId, meldingskode))
        verify(exactly = 1) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify { acknowledgment.acknowledge() }
    }

    @Test
    fun `verifiser at hendelsen ikke ackes ved database-feil`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "1234"

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = Meldingskode.SIVILSTAND
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        // Mock repository for aa kaste exception
        val failingRepository = mockk<PersonEndringRepository>()
        val failingListener = PersonEndringListener(failingRepository, personHendelseRepository, sporingsloggProducer, objectMapper)

        assertThrows<RuntimeException> {
            failingListener.listener(hendelseJson, consumerRecord, acknowledgment)
        }
        verify(exactly = 0) { kafkaTemplate.send(any<String>(), any<String>()) }
        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal ikke acke invalid JSON`() {
        val invalidJson = "{ invalid json }"
        val consumerRecord = mockConsumerRecord(invalidJson)

        listener.listener(invalidJson, consumerRecord, acknowledgment)

        verify(exactly = 0) { acknowledgment.acknowledge() }

        val saved = personEndringRepository.findAll()
        assertEquals(0, saved.size)
    }

    @Test
    fun `skal lagre alle meldingskoder`() {
        val fnr = "12345678901"
        val tpnr = "1234"

        Meldingskode.entries.forEach { meldingskode ->
            val hendelseId = UUID.randomUUID().toString()
            val hendelse = PersonEndringKafkaHendelse(
                hendelseId = hendelseId,
                tpNr = listOf(tpnr),
                fnr = fnr,
                meldingsKode = meldingskode,
                sivilstand = if (meldingskode == Meldingskode.SIVILSTAND) "GIFT" else null,
                sivilstandDato = if (meldingskode == Meldingskode.SIVILSTAND) LocalDate.now() else null,
                dodsdato = if (meldingskode == Meldingskode.DOEDSFALL) LocalDate.now() else null
            )

            val hendelseJson = objectMapper.writeValueAsString(hendelse)
            val consumerRecord = mockConsumerRecord(hendelseJson)
            listener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        val saved = personEndringRepository.findAll()
        assertEquals(4, saved.size)

        Meldingskode.entries.forEach { meldingskode ->
            assertTrue(saved.any { it.meldingskode == meldingskode })
        }
    }

    private fun mockConsumerRecord(value: String): ConsumerRecord<String, String> {
        return ConsumerRecord(
            "PERSON_ENDRING",
            0,
            1L,
            UUID.randomUUID().toString(),
            value
        )
    }
}
