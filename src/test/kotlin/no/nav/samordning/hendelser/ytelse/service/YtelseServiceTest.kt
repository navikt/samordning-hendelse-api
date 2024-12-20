package no.nav.samordning.hendelser.ytelse.service

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.nav.samordning.hendelser.TestData
import no.nav.samordning.hendelser.database.DatabaseConfig
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import kotlin.test.Test

@DataJpaTest
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@Import(DatabaseConfig::class, YtelseService::class)
class YtelseServiceTest {

    @Autowired
    private lateinit var ytelseService: YtelseService

    @Autowired
    private lateinit var ytelseRepository: YtelseHendelserRepository

    @Test
    fun `hent hendelse med rett sekvensnummer`() {
        val forventetHendelseYtelse = TestData.hy3010
        val hendelseYtelseList = ytelseService.fetchSeqAndYtelseHendelser("3200",0,0,10)

        assertThat(hendelseYtelseList.size, equalTo(1))
        assertThat(hendelseYtelseList[0],samePropertyValuesAs(forventetHendelseYtelse)
        )
    }

    @Test
    fun `hent hendelse med sekvensnummer fom 2 og antall er 2`() {
        val forventetHendelseYtelse1 = TestData.hy3400_2
        val forventetHendelseYtelse2 = TestData.hy3400_3
        val hendelseYtelseList = ytelseService.fetchSeqAndYtelseHendelser("3030",2,0,10)

        assertThat(hendelseYtelseList.size, equalTo(2))
        assertThat(hendelseYtelseList[0],samePropertyValuesAs(forventetHendelseYtelse1))
        assertThat(hendelseYtelseList[1],samePropertyValuesAs(forventetHendelseYtelse2))
    }
}