package no.nav.samordning.hendelser.person

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.kafka.PersonEndringKafkaHendelse
import no.nav.samordning.hendelser.person.kafka.PersonEndringListener
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import no.nav.samordning.hendelser.person.repository.PersonHendelseRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
class PersonEndringListenerTest {

    @Autowired
    private lateinit var personEndringRepository: PersonEndringRepository

    @Autowired
    private lateinit var personHendelseRepository: PersonHendelseRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var listener: PersonEndringListener
    private lateinit var acknowledgment: Acknowledgment

    @BeforeEach
    fun setup() {
        listener = PersonEndringListener(personEndringRepository, personHendelseRepository, objectMapper)
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
    fun `skal lagre personendring med enkelt tpnr`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "123"
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

        verify { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal lagre personendring med flere tpnr`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnrList = listOf("123", "456", "789")
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

        verify { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal sette sekvensnummer basert på siste sekvensnummer for tpnr`() {
        val tpnr = "123"
        val fnr = "12345678901"

        // Lagre to tidligere hendelser
        repeat(2) { i ->
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

        val saved = personEndringRepository.getFirstByTpnrOrderBySekvensnummerDesc(tpnr)
        assertNotNull(saved)
        assertEquals(3L, saved.sekvensnummer)
    }

    @Test
    fun `skal ignorere duplikat hendelse basert på hendelseId og meldingskode`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "123"
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

        verify(exactly = 2) { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal lagre personHendelse med hendelseId og meldingskode`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "123"
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

        verify { acknowledgment.acknowledge() }
    }

    @Test
    fun `verifiser at hendelsen ikke ackes ved database-feil`() {
        val hendelseId = UUID.randomUUID().toString()
        val fnr = "12345678901"
        val tpnr = "123"

        val hendelse = PersonEndringKafkaHendelse(
            hendelseId = hendelseId,
            tpNr = listOf(tpnr),
            fnr = fnr,
            meldingsKode = Meldingskode.SIVILSTAND
        )

        val hendelseJson = objectMapper.writeValueAsString(hendelse)
        val consumerRecord = mockConsumerRecord(hendelseJson)

        // Mock repository for å kaste exception
        val failingRepository = mockk<PersonEndringRepository>()
        val failingListener = PersonEndringListener(failingRepository, personHendelseRepository, objectMapper)

        every { failingRepository.getFirstByTpnrOrderBySekvensnummerDesc(any()) } throws RuntimeException("Database error")

        assertThrows<RuntimeException> {
            failingListener.listener(hendelseJson, consumerRecord, acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    @Test
    fun `skal håndtere invalid JSON med ack`() {
        val invalidJson = "{ invalid json }"
        val consumerRecord = mockConsumerRecord(invalidJson)

        listener.listener(invalidJson, consumerRecord, acknowledgment)

        verify { acknowledgment.acknowledge() }

        val saved = personEndringRepository.findAll()
        assertEquals(0, saved.size)
    }

    @Test
    fun `skal lagre alle meldingskoder`() {
        val fnr = "12345678901"
        val tpnr = "123"

        Meldingskode.values().forEach { meldingskode ->
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

        Meldingskode.values().forEach { meldingskode ->
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