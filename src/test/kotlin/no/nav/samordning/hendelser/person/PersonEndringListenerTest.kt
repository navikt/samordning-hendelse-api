package no.nav.samordning.hendelser.person

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.kafka.PersonEndringKafkaHendelse
import no.nav.samordning.hendelser.person.kafka.PersonEndringListener
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import no.nav.samordning.hendelser.person.repository.PersonHendelseRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import java.io.IOException
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
class PersonEndringListenerTest {

    private val mapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule())
    private val debugLogger: Logger = LoggerFactory.getLogger("no.nav.samordning.hendelser") as Logger
    private val listAppender = ListAppender<ILoggingEvent>()

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)

    @Autowired
    private lateinit var listener : PersonEndringListener

    @Autowired
    private lateinit var personEndringRepository: PersonEndringRepository

    @Autowired
    private lateinit var personHendelseRepository: PersonHendelseRepository

    @BeforeEach
    fun setup() {
        listAppender.start()
        debugLogger.addAppender(listAppender)
    }

    @AfterEach
    fun cleanup() {
        listAppender.stop()
    }

    @Test
    fun `test av hendelse med et tpnr i seg`() {
        val personEndringHendelse =  mockPersonEndringKafkaHendelse(tpnr = listOf("3090"))
        val personEndringHendelseJson = mapper.writeValueAsString(personEndringHendelse)

        listener.listener(personEndringHendelseJson, mockk(relaxed = true), acknowledgment)

        assertEquals(1, personEndringRepository.countAllByTpnr("3090"))
        assertEquals(true, personHendelseRepository.existsByHendelseIdAndMeldingskode(personEndringHendelse.hendelseId, personEndringHendelse.meldingsKode))

        //test på skal ikke behandles på nytt. (samme hendelseid)
        listener.listener(personEndringHendelseJson, mockk(relaxed = true), acknowledgment)
        assertEquals(1, personEndringRepository.countAllByTpnr("3090"))

        verify(exactly = 2) { acknowledgment.acknowledge() }
    }

    @Test
    fun `test av hendelser med flere tpnr i seg`() {
        val personEndringHendelser1 =  mockPersonEndringKafkaHendelse(tpnr = listOf("3010", "3200", "3400"))
        val personEndringHendelser2 =  mockPersonEndringKafkaHendelse(tpnr = listOf("3200", "3400"))
        val personEndringHendelse1Json = mapper.writeValueAsString(personEndringHendelser1)
        val personEndringHendelse2Json = mapper.writeValueAsString(personEndringHendelser2)

        listener.listener(personEndringHendelse1Json, mockk(relaxed = true), acknowledgment)
        listener.listener(personEndringHendelse2Json, mockk(relaxed = true), acknowledgment)

        assertEquals(1, personEndringRepository.countAllByTpnr("3010"))
        assertEquals(2, personEndringRepository.countAllByTpnr("3200"))
        assertEquals(2, personEndringRepository.countAllByTpnr("3400"))
        assertEquals(true, personHendelseRepository.existsByHendelseIdAndMeldingskode(personEndringHendelser1.hendelseId, personEndringHendelser1.meldingsKode))
        assertEquals(true, personHendelseRepository.existsByHendelseIdAndMeldingskode(personEndringHendelser2.hendelseId, personEndringHendelser2.meldingsKode))

        verify(exactly = 2) { acknowledgment.acknowledge() }
    }

    @Test
    fun `verifiser at hendelsen ikke ackes ved database-feil`() {
        val personEndringHendelse =  mockPersonEndringKafkaHendelse(tpnr = listOf("3010"))
        val personEndringHendelseJson = mapper.writeValueAsString(personEndringHendelse)

        val personEndringRepository = mockk<PersonEndringRepository>(relaxed = true)
        val personEndringListener = PersonEndringListener(personEndringRepository, mockk(relaxed = true))
        every { personEndringRepository.save(any()) } returnsArgument 0
        every { personEndringRepository.flush() } throws IOException("IO error")

        assertThrows<IOException> {
            personEndringListener.listener(personEndringHendelseJson, mockk(relaxed = true), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge()  }
        verify(exactly = 1) { personEndringRepository.save(any()) }
        verify(exactly = 1) { personEndringRepository.flush() }
    }


    fun mockPersonEndringKafkaHendelse(tpnr: List<String>, fnr: String = "1001010101") = PersonEndringKafkaHendelse(
        hendelseId = UUID.randomUUID().toString(),
        tpNr = tpnr,
        fnr = fnr,
        sivilstand = "GIFT",
        sivilstandDato = LocalDate.now(),
        meldingsKode = Meldingskode.SIVILSTAND


    )
}