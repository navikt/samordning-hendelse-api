package no.nav.samordning.hendelser.vedtak.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseContainerDO
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepositoryDO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.Acknowledgment
import java.io.IOException

internal class VarsleVedtakSamordningListenerTest {

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)
    private val hendelseRepository = mockk<HendelseRepositoryDO>(relaxed = true)
    private val mapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule())
    private val listener =  VarsleVedtakSamordningListener(hendelseRepository)

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
    fun testingAvConsumerMed_UFORE_SAERALDERP() {

        val samHendelse = mockSamHendelse("UFORE_SAERALDERP")

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

    private fun mockSamHendelse(ytelse: String = "ALDER") =
        SamHendelse(
        "3030",
        ytelse,
        "12345678901",
        "123123","32321",
        "2022-10-09",
        null
    )



}

fun SamHendelse.toJson() = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule()).writeValueAsString(this)