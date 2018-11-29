package no.nav.samordning.hendelser.hendelse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class Database {
    private static final String SQL_GOTTA_FETCH_THEM_ALL = "Select b.ytelsesType, b.identifikator, b.vedtakId, b.fom, b.tom FROM (Select a.ytelsesType, a.identifikator, a.vedtakId, a.fom, a.tom, rownum rn FROM (Select ytelsesType, identifikator, vedtakId, fom, tom FROM T_SAMORDNINGSPLIKTIG_VEDTAK order by id) a WHERE (rownum < ((?*?) + 1) )) b WHERE (rn >= (((?-1)*?) + 1))";
    private static final String SQL_INSERT_RECORD = "insert into T_SAMORDNINGSPLIKTIG_VEDTAK(ytelsesType, identifikator, vedtakId, fom, tom) values(?,?,?, ?,?)";

    private JdbcTemplate database;

    @Autowired
    public Database(JdbcTemplate database) {
        this.database = database;
    }

    public List<Hendelse> fetchAll(Integer side, Integer antall){
        return database.query(SQL_GOTTA_FETCH_THEM_ALL, BeanPropertyRowMapper.newInstance(Hendelse.class), side, antall, side, antall);
    }

    public void insert(Hendelse hendelse){

        Date fom = Date.valueOf(hendelse.getFom());
        Date tom = hendelse.getTom() == null ? null : Date.valueOf(hendelse.getTom());
        database.update(SQL_INSERT_RECORD, hendelse.getYtelsesType(), hendelse.getIdentifikator(), hendelse.getVedtakId(), fom, tom);
    }
}