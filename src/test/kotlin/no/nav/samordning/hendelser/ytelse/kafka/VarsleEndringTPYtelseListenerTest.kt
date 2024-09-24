package no.nav.samordning.hendelser.ytelse.kafka

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

    private val hendelseJson = """{"uuid":"41383630-3243-3644-3837-314334384332","sent":false,"sekvensnummer":1,"tpnr":"3010","fnr":"14087412334","hendelseType":"OPPRETT","ytelseType":"UFORE","datoFom":"2024-01-01 12:12:12","datoTom":null}"""

    @Test
    fun testingAvConsumer() {
        val ytelseHendelseDto = mockYtelseHendelse()
        val ytelseHendelse = YtelseHendelse(ytelseHendelseDto)

        every { ytelseHendelserRepository.saveAndFlush(any()) } returns ytelseHendelse

        listener.listener(hendelseJson, mockk(relaxed = true), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
        verify(exactly = 1) { ytelseHendelserRepository.saveAndFlush(any()) }
    }

    @Test
    fun testingAvConsumerFeilerVedLagring() {

        every { ytelseHendelserRepository.saveAndFlush(any()) } throws IOException("IO error")

        assertThrows<IOException> {
            listener.listener(hendelseJson, mockk(relaxed = true), acknowledgment)
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