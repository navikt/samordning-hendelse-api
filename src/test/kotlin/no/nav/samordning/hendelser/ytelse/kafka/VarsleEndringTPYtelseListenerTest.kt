package no.nav.samordning.hendelser.ytelse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseDTO
import no.nav.samordning.hendelser.ytelse.domain.YtelseTypeCode
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelse
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.Acknowledgment
import java.io.IOException
import java.time.LocalDateTime

class VarsleEndringTPYtelseListenerTest {

    private val acknowledgment: Acknowledgment = mockk(relaxed = true)
    private val ytelseHendelserRepository = mockk<YtelseHendelserRepository>(relaxed = true)
    private val listener = VarsleEndringTPYtelseListener(ytelseHendelserRepository)

    @Test
    fun testingAvConsumer() {
        val ytelseHendelseDto = mockYtelseHendelse()
        val ytelseHendelse = YtelseHendelse(ytelseHendelseDto)

        every { ytelseHendelserRepository.saveAndFlush(any()) } returns ytelseHendelse

        listener.listener(ytelseHendelseDto.toJson(), mockk(relaxed = true), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
        verify(exactly = 1) { ytelseHendelserRepository.saveAndFlush(any()) }
    }

    @Test
    fun testingAvConsumerFeilerVedLagring() {

        every { ytelseHendelserRepository.saveAndFlush(any()) } throws IOException("IO error")

        assertThrows<IOException> {
            listener.listener(mockYtelseHendelse().toJson(), mockk(relaxed = true), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge()  }
        verify(exactly = 1) { ytelseHendelserRepository.saveAndFlush(any()) }

    }

    private fun mockYtelseHendelse() =
        YtelseHendelseDTO(
            1L,
            "3010",
            "14087412334",
            HendelseTypeCode.OPPRETT,
            YtelseTypeCode.UFORE,
            LocalDateTime.of(2024,1,1, 12, 12, 12),
            null
        )

}

fun YtelseHendelseDTO.toJson(): String = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule()).writeValueAsString(this)