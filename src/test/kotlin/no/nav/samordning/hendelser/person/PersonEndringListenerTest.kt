package no.nav.samordning.hendelser.person

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import java.time.LocalDate
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

    //private val personEndringHendelseJson = mapper.writeValueAsString(mockPersonEndringKafkaHendelse())
    //private val personEndringHendelseJson1 = mapper.writeValueAsString(mockPersonEndringKafkaHendelse("3020"))

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
    fun testingAvConsumer() {
        val personEndringHendelseSPK3010 =  mockPersonEndringKafkaHendelse(tpnr = "3010")
        val personEndringHendelseJson = mapper.writeValueAsString(personEndringHendelseSPK3010)

        listener.listener(personEndringHendelseJson, mockk(relaxed = true), acknowledgment)

        assertEquals(1, personEndringRepository.countAllByTpnr("3010"))
        assertEquals(true, personHendelseRepository.existsByHendelseIdAndMeldingskode(personEndringHendelseSPK3010.hendelseId, personEndringHendelseSPK3010.meldingsKode))

        //test på skal ikke behandles på nytt. (samme hendelseid)
        listener.listener(personEndringHendelseJson, mockk(relaxed = true), acknowledgment)
        assertEquals(1, personEndringRepository.countAllByTpnr("3010"))


        verify(exactly = 2) { acknowledgment.acknowledge() }
    }

//    @Test
//    fun testingAvConsumerFeilerVedLagring() {
//        val ytelseHendelserRepository = mockk<YtelseHendelserRepository>(relaxed = true)
//        val listener2 = VarsleEndringTPYtelseListener(ytelseHendelserRepository)
//        every { ytelseHendelserRepository.save(any()) } returnsArgument 0
//        every { ytelseHendelserRepository.flush() } throws IOException("IO error")
//
//        assertThrows<IOException> {
//            listener2.listener(hendelseJson, mockk(relaxed = true), acknowledgment)
//        }
//
//        verify(exactly = 0) { acknowledgment.acknowledge()  }
//        verify(exactly = 2) {
//            ytelseHendelserRepository.save(any()) }
//        verify(exactly = 1) { ytelseHendelserRepository.flush() }
//    }


    fun mockPersonEndringKafkaHendelse(tpnr: String, fnr: String = "1001010101") = PersonEndringKafkaHendelse(
        hendelseId = "1",
        tpNr = listOf(tpnr),
        fnr = fnr,
        sivilstand = "GIFT",
        sivilstandDato = LocalDate.now(),
        meldingsKode = Meldingskode.SIVILSTAND


    )
}