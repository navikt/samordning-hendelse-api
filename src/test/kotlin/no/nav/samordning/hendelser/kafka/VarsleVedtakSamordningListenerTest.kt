package no.nav.samordning.hendelser.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.samordning.hendelser.hendelse.HendelseContainer
import no.nav.samordning.hendelser.hendelse.HendelseRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.Acknowledgment
import java.io.IOException
import java.time.LocalDate

internal class VarsleVedtakSamordningListenerTest {

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)
    private val hendelseRepository = mockk<HendelseRepository>(relaxed = true)
    private val listener =  VarsleVedtakSamordningListener(hendelseRepository)

    @BeforeEach
    fun setup() {

    }

    @Test
    fun testingAvConsumer() {

        val samHendelse = mockSamHendelse()

        val container = HendelseContainer(samHendelse)

        every { hendelseRepository.saveAndFlush(any()) } returns container
        listener.listener(samHendelse, mockk(relaxed = true), acknowledgment)
        verify(exactly = 1) { acknowledgment.acknowledge()  }
        verify(exactly = 1) { hendelseRepository.saveAndFlush(any()) }

    }


    @Test
    fun testingAvConsumerFeilerVedLagring() {


        every { hendelseRepository.saveAndFlush(any()) } throws IOException("IO error")

        assertThrows<IOException> {
            listener.listener(mockSamHendelse(), mockk(relaxed = true), acknowledgment)
        }
        verify(exactly = 0) { acknowledgment.acknowledge()  }
        verify(exactly = 1) { hendelseRepository.saveAndFlush(any()) }

    }

    private fun mockSamHendelse() =
        SamHendelse(
        "3030",
        "ALDER",
        "12345678901",
        "123123","32321",
        LocalDate.of(2022,10,9),
        null
    )


}
