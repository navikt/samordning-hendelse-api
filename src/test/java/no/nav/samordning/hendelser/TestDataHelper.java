package no.nav.samordning.hendelser;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.json.bind.JsonbBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class TestDataHelper {

    private final List<Hendelse> hendelser;

    public TestDataHelper(JdbcTemplate database) {
        hendelser = database.queryForList("SELECT HENDELSE_DATA FROM HENDELSER", PGobject.class)
            .stream().map(hendelse -> JsonbBuilder.create().fromJson(hendelse.getValue(), Hendelse.class))
            .collect(Collectors.toList());
    }

    public Hendelse hendelse(String vedtakId) {
        for (Hendelse hendelse : hendelser) {
            if (hendelse.getVedtakId().equals(vedtakId))
                return hendelse;
        }
        return null;
    }

    public List<String> hendelseIdList(String... vedtakIdList) {
        return hendelser.stream()
            .filter(hendelse -> Arrays.asList(vedtakIdList).contains(hendelse.getVedtakId()))
            .map(Hendelse::getIdentifikator).collect(Collectors.toList());
    }

    public List<Hendelse> mapJsonToHendelser(String body) throws Exception {
        JSONArray data = new JSONObject(body).getJSONArray("hendelser");
        List<Hendelse> hendelser = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject hendelse = data.getJSONObject(i);
            var tom = hendelse.getString("tom");
            if (tom.equals("null")) tom = null;
            hendelser.add(new Hendelse(
                hendelse.getString("identifikator"),
                hendelse.getString("ytelsesType"),
                hendelse.getString("vedtakId"),
                hendelse.getString("fom"), tom
            ));
        }
        return hendelser;
    }
}