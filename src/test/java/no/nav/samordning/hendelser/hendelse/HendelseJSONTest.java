package no.nav.samordning.hendelser.hendelse;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

public class HendelseJSONTest {

    @Test
    public void jsonToObject() {
        Hendelse hendelse = new Hendelse();
        hendelse.setYtelsesType("AAP");
        hendelse.setIdentifikator("12345678901");
        hendelse.setVedtakId("ABC123");
        hendelse.setFom(LocalDate.of(2020, 01, 01));
        hendelse.setTom(LocalDate.of(2025, 01, 01));

        Jsonb jsonb = JsonbBuilder.create();
        String json = "{\"ytelsesType\":\"AAP\",\"identifikator\":\"12345678901\",\"vedtakId\":\"ABC123\",\"fom\":\"2020-01-01\",\"tom\":\"2025-01-01\"}";

        Hendelse hendelse2 = jsonb.fromJson(json, Hendelse.class);
        assertThat(hendelse, samePropertyValuesAs(hendelse2));
    }

    @Test
    public void objectToJSON() {
        Hendelse hendelse = new Hendelse();
        hendelse.setYtelsesType("AAP");
        hendelse.setIdentifikator("12345678901");
        hendelse.setVedtakId("ABC123");
        hendelse.setFom(LocalDate.of(2020, 01, 01));
        hendelse.setTom(LocalDate.of(2025, 01, 01));

        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(hendelse);

        String excpected = "{\"ytelsesType\":\"AAP\",\"identifikator\":\"12345678901\",\"vedtakId\":\"ABC123\",\"fom\":\"2020-01-01\",\"tom\":\"2025-01-01\"}";

        List<String> excpectedList = JsonPath.read(excpected, "$.*");
        List<String> resultList = JsonPath.read(result, "$.*");

        assertThat((excpectedList),
                Matchers.containsInAnyOrder(resultList.toArray()));
    }
}
