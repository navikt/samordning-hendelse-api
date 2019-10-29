package no.nav.samordning.hendelser;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.json.bind.JsonbBuilder;
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

    public Hendelse hendelse(String identifikator) {
        for (Hendelse hendelse : hendelser)
            if (hendelse.getIdentifikator().equals(identifikator))
                return hendelse;
        return null;
    }

    public List<Hendelse> hendelser(String... identifikator) {
        return hendelser.stream()
            .filter(hendelse -> Arrays.asList(identifikator).contains(hendelse.getIdentifikator()))
            .collect(Collectors.toList());
    }
}