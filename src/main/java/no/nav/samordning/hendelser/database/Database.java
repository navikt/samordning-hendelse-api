package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.json.bind.JsonbBuilder;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class Database {
    private final String SQL_FETCH_HENDELSER = "SELECT HENDELSE_DATA FROM HENDELSER WHERE ID >= ? OFFSET ? LIMIT ?";
    private final String SQL_TOTAL_COUNT     = "SELECT COUNT(HENDELSE_DATA) FROM HENDELSER";
    private JdbcTemplate database;

    public Database(JdbcTemplate database) {
        this.database = database;
    }

    public List<Hendelse> fetch(int side, int antall, int sekvensnummer) {
        return database.queryForList(SQL_FETCH_HENDELSER, PGobject.class, sekvensnummer, side * antall, antall)
            .stream().map(hendelse -> JsonbBuilder.create().fromJson(hendelse.getValue(), Hendelse.class))
            .collect(Collectors.toList());
    }

    public int getNumberOfPages(int antall) {
        int numberOfPages = 0;
        try {
            var total = Integer.parseInt(database.queryForObject(SQL_TOTAL_COUNT, String.class));
            numberOfPages = (total + antall - 1) / antall;
        } catch (Exception e) {}
        return numberOfPages;
    }
}