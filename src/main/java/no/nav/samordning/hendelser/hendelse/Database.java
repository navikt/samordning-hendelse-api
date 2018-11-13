package no.nav.samordning.hendelser.hendelse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

import no.nav.samordning.hendelser.Metrics;

@Repository
public class Database {
    private static final String SQL_GOTTA_FETCH_THEM_ALL = "Select ytelsesType, identifikator, vedtakId, fom, tom FROM T_SAMORDNINGSPLIKTIG_VEDTAK";
    private static final String SQL_INSERT_RECORD = "insert into T_SAMORDNINGSPLIKTIG_VEDTAK(ytelsesType, identifikator, vedtakId, fom, tom) values(?,?,?, ?,?)";

    private JdbcTemplate database;

    @Autowired
    public Database(JdbcTemplate database) {
        this.database = database;
    }

    public List<Hendelse> fetchAll(){
        return database.query(SQL_GOTTA_FETCH_THEM_ALL, BeanPropertyRowMapper.newInstance(Hendelse.class), null);
    }

    public void insert(Hendelse hendelse){

        Date fom = Date.valueOf(hendelse.getFom());
        Date tom = hendelse.getTom() == null ? null : Date.valueOf(hendelse.getTom());
        database.update(SQL_INSERT_RECORD, hendelse.getYtelsesType(), hendelse.getIdentifikator(), hendelse.getVedtakId(), fom, tom);

        Metrics.incAntallHendelser();
    }
}