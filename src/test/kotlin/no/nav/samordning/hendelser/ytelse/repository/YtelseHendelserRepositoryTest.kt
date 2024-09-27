package no.nav.samordning.hendelser.ytelse.repository

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime
import kotlin.test.assertEquals

@DataJpaTest
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
class YtelseHendelserRepositoryTest {

    @Autowired
    private lateinit var ytelseHendelserRepository: YtelseHendelserRepository

    @Test
    fun `when getFirstByTpnrOrderBySekvensnummerDesc then return siste hendelse`() {
        val expectedYtelseHendelse = YtelseHendelse(id = 3L, sekvensnummer = 2L,  tpnr = "3200", mottaker = "3010", identifikator = "14087459999", hendelseType = HendelseTypeCode.OPPRETT, ytelseType = "ALDER", datoBrukFom = LocalDateTime.of(2024, 1, 1, 12, 12,12), datoBrukTom = null)

        val actualHendelse = ytelseHendelserRepository.getFirstByMottakerOrderBySekvensnummerDesc("3010")

        assertNotNull(actualHendelse)
        assertEquals(expectedYtelseHendelse, actualHendelse)

    }

    @Test
    fun `when countAllByTpnr then return correct count`() {
        val expectedCount = 2L

        val actualCount = ytelseHendelserRepository.countAllByMottaker("3010")

        assertNotNull(actualCount)
        assertEquals(expectedCount, actualCount)
    }



}