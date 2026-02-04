package no.nav.samordning.hendelser.vedtak.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseContainerDO
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepositoryDO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.Acknowledgment
import tools.jackson.databind.ObjectMapper
import tools.jackson.datatype.jsr310.JavaTimeModule
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.io.IOException

internal class VarsleVedtakSamordningListenerTest {

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)
    private val hendelseRepository = mockk<HendelseRepositoryDO>(relaxed = true)
    private val mapper : ObjectMapper = jacksonMapperBuilder().addModule(JavaTimeModule()).build()
    private val listener =  VarsleVedtakSamordningListener(hendelseRepository, mapper)

    @BeforeEach
    fun setup() {

    }

    @Test
    fun testingAvConsumer() {

        val samHendelse = mockSamHendelse()

        val container = HendelseContainerDO(samHendelse)

        every { hendelseRepository.saveAndFlush(any()) } returns container
        listener.listener(samHendelse.toJson(), mockk(relaxed = true), acknowledgment)
        verify(exactly = 1) { acknowledgment.acknowledge()  }
        verify(exactly = 1) { hendelseRepository.saveAndFlush(any()) }

    }


    @Test
    fun testingAvConsumerFeilerVedLagring() {


        every { hendelseRepository.saveAndFlush(any()) } throws IOException("IO error")

        assertThrows<IOException> {
            listener.listener(mockSamHendelse().toJson(), mockk(relaxed = true), acknowledgment)
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
        "2022-10-09",
        null
    )


    fun SamHendelse.toJson() = mapper.writeValueAsString(this)

}
