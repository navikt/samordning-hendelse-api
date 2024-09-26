package no.nav.samordning.hendelser.ytelse.kafka

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import java.io.IOException
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
class VarsleEndringTPYtelseListenerTest {

    private val debugLogger: Logger = LoggerFactory.getLogger("no.nav.samordning.hendelser") as Logger
    private val listAppender = ListAppender<ILoggingEvent>()

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)

    @Autowired
    private lateinit var listener : VarsleEndringTPYtelseListener

    private val hendelseJson = """{"uuid":"41383630-3243-3644-3837-314334384332","sent":false,"tpnr":"3010","mottakere":["3200", "3060"],"fnr":"14087412334","hendelseType":"OPPRETT","ytelseType":"UFORE","datoFom":"2024-01-01 12:12:12","datoTom":null}"""
    private val hendelseJson1 = """{"uuid":"41383630-3243-3644-3837-314334384333","sent":false,"tpnr":"3200","mottakere":["3010", "3060"],"fnr":"14087412334","hendelseType":"OPPRETT","ytelseType":"SAERALDER","datoFom":"2024-01-01 12:12:12","datoTom":null}"""

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
        listener.listener(hendelseJson, mockk(relaxed = true), acknowledgment)
        listener.listener(hendelseJson1, mockk(relaxed = true), acknowledgment)

        assertTrue(sjekkLoggingFinnes("mottaker-sekvensnummer: {3200=2, 3060=1}"))
        assertTrue(sjekkLoggingFinnes("mottaker-sekvensnummer: {3010=3, 3060=2}"))

        verify(exactly = 2) { acknowledgment.acknowledge() }
    }

    @Test
    fun testingAvConsumerFeilerVedLagring() {
        val ytelseHendelserRepository = mockk<YtelseHendelserRepository>(relaxed = true)
        val listener2 = VarsleEndringTPYtelseListener(ytelseHendelserRepository)
        every { ytelseHendelserRepository.save(any()) } returnsArgument 0
        every { ytelseHendelserRepository.flush() } throws IOException("IO error")

        assertThrows<IOException> {
            listener2.listener(hendelseJson, mockk(relaxed = true), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge()  }
        verify(exactly = 2) {
            ytelseHendelserRepository.save(any()) }
        verify(exactly = 1) { ytelseHendelserRepository.flush() }
    }

    private fun sjekkLoggingFinnes(keywords: String): Boolean {
        val logsList: List<ILoggingEvent> = listAppender.list
        val result : String? = logsList.find { message ->
            message.message.contains(keywords)
        }?.message
        return result?.contains(keywords) ?: false
    }

}