package no.nav.samordning.hendelser;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.json.bind.JsonbBuilder;
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
}