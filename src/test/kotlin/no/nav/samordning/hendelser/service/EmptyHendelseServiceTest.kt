package no.nav.samordning.hendelser.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.samordning.hendelser.database.DatabaseConfig
import no.nav.samordning.hendelser.hendelse.HendelseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatabaseConfig::class, HendelseService::class])
class EmptyHendelseServiceTest {

    @MockkBean
    private lateinit var hendelseRepository: HendelseRepository

    @Autowired
    private lateinit var hendelseService: HendelseService

    @Test
    fun null_count_returns_0(){
        every { hendelseRepository.countAllByTpnrAndYtelsesType(any(), any()) } returns 0
        assertEquals(0, hendelseService.getNumberOfPages("1000", 1, 0))
    }
}
