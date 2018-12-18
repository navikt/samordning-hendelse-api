package no.nav.samordning.hendelser.hendelse;

import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyOrderStrategy;
import java.time.LocalDate;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
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

        JsonbConfig config = new JsonbConfig()
                .withPropertyOrderStrategy(PropertyOrderStrategy.ANY);

        Jsonb jsonb = JsonbBuilder.create(config);
        String result = jsonb.toJson(hendelse);

        String excpected = "{\"ytelsesType\":\"AAP\",\"identifikator\":\"12345678901\",\"vedtakId\":\"ABC123\",\"fom\":\"2020-01-01\",\"tom\":\"2025-01-01\"}";
        assertEquals(excpected, result);
    }
}
