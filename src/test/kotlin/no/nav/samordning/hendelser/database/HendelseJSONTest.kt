package no.nav.samordning.hendelser.database

import com.jayway.jsonpath.JsonPath
import no.nav.samordning.hendelser.hendelse.Hendelse
import org.hamcrest.Matchers
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.Assert.assertThat
import org.junit.jupiter.api.Test
import javax.json.bind.JsonbBuilder

class HendelseJSONTest {

    @Test
    fun jsonToObject() {
        val hendelse = Hendelse().apply {
            ytelsesType = "AAP"
            identifikator = "12345678901"
            vedtakId = "ABC123"
            samId = "BOGUS"
            fom = "2020-01-01"
            tom = "2025-01-01"
        }

        val json = """{"ytelsesType":"AAP","identifikator":"12345678901","vedtakId":"ABC123","samId":"BOGUS","fom":"2020-01-01","tom":"2025-01-01"}"""

        val hendelse2 = JsonbBuilder.create().fromJson(json, Hendelse::class.java)
        assertThat(hendelse, samePropertyValuesAs(hendelse2))
    }

    @Test
    fun objectToJSON() {
        val hendelse = Hendelse().apply {
            ytelsesType = "AAP"
            identifikator = "12345678901"
            vedtakId = "ABC123"
            samId = "BOGUS"
            fom = "2020-01-01"
            tom = "2025-01-01"
        }

        val result = JsonbBuilder.create().toJson(hendelse)

        val excpected = """{"ytelsesType":"AAP","identifikator":"12345678901","vedtakId":"ABC123","samId":"BOGUS","fom":"2020-01-01","tom":"2025-01-01"}"""

        val excpectedList = JsonPath.read<List<String>>(excpected, "$.*")
        val resultList = JsonPath.read<List<String>>(result, "$.*")

        assertThat(excpectedList,
                Matchers.containsInAnyOrder<Any>(*resultList.toTypedArray()))
    }
}
