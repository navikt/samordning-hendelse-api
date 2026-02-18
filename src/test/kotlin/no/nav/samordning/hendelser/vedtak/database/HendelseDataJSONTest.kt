package no.nav.samordning.hendelser.vedtak.database

import no.nav.samordning.hendelser.config.IntegrationTest
import no.nav.samordning.hendelser.vedtak.hendelse.Hendelse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@IntegrationTest
class HendelseDataJSONTest {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun jsonToObject() {
        val hendelse = Hendelse(
            ytelsesType = "AAP",
            identifikator = "12345678901",
            vedtakId = "ABC123",
            samId = "BOGUS",
            fom = LocalDate.of(2020, 1, 1),
            tom = LocalDate.of(2025, 1, 1),
        )

        val json = """{"ytelsesType":"AAP","identifikator":"12345678901","vedtakId":"ABC123","samId":"BOGUS","fom":"2020-01-01","tom":"2025-01-01"}"""

        val hendelse2 = mapper.readValue(json, Hendelse::class.java)
        assertThat(hendelse, samePropertyValuesAs(hendelse2))
    }

    @Test
    fun objectToJSON() {
        val hendelse = Hendelse(
            ytelsesType = "AAP",
            identifikator = "12345678901",
            vedtakId = "ABC123",
            samId = "BOGUS",
            fom = LocalDate.of(2020,1,1),
            tom = LocalDate.of(2025,1,1)
        )

        val result = mapper.writeValueAsString(hendelse)

        val expected = """{"ytelsesType":"AAP","identifikator":"12345678901","vedtakId":"ABC123","samId":"BOGUS","fom":"2020-01-01","tom":"2025-01-01"}"""


        assertEquals (mapper.readTree(expected), mapper.readTree(result))
    }
}
