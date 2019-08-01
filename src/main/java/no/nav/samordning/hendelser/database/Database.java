package no.nav.samordning.hendelser.database;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.json.bind.JsonbBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class Database {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Hendelse> fetchHendelser(String tpnr, int offset, int side, int antall) {
        var sql = "SELECT HENDELSE_DATA " +
            "FROM HENDELSER WHERE ID >= ? " +
            "AND TPNR = ? " +
            "ORDER BY ID " +
            "OFFSET ? LIMIT ?";

        return jdbcTemplate.queryForList(sql, PGobject.class, offset, tpnr, side * antall, antall)
            .stream().map(hendelse -> JsonbBuilder.create().fromJson(hendelse.getValue(), Hendelse.class))
            .collect(Collectors.toList());
    }

    public int getNumberOfPages(String tpnr, int antall) {
        var sql = "SELECT COUNT(HENDELSE_DATA) FROM HENDELSER WHERE TPNR = ? ";
        int numberOfPages = 0;
        try {
            var total = Integer.parseInt(Objects.requireNonNull(jdbcTemplate.queryForObject(sql, new Object[]{tpnr}, String.class)));
            numberOfPages = (total + antall - 1) / antall;
        } catch (Exception ignored) { }
        return numberOfPages;
    }
}