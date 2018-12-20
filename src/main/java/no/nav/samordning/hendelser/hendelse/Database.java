package no.nav.samordning.hendelser.hendelse;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class Database {
    private static final String SQL_GOTTA_FETCH_THEM_ALL = "SELECT * FROM T_SAMORDNINGSPLIKTIG_VEDTAK";
    private static final String SQL_INSERT_RECORD = "INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES('?')";

    private JdbcTemplate database;

    @Autowired
    public Database(JdbcTemplate database) {
        this.database = database;
    }

    public List<Hendelse> fetchAll(Integer side, Integer antall){
        List<Hendelse> hendelser = new ArrayList<>();
        List<PGobject> jsonHendelser = database.queryForList(SQL_GOTTA_FETCH_THEM_ALL, PGobject.class);

        Jsonb jsonb = JsonbBuilder.create();

        for (PGobject jsonHendelse : jsonHendelser) {
            System.out.println("VALUE: " + jsonHendelse.getValue());
            hendelser.add(jsonb.fromJson(jsonHendelse.getValue(), Hendelse.class));
        }

        return hendelser;
    }

    public void insert(Hendelse hendelse) throws SQLException {
        Jsonb jsonb = JsonbBuilder.create();
        PGobject data = new PGobject();
        data.setType("jsonb");
        data.setValue(jsonb.toJson(hendelse));

        database.update(SQL_INSERT_RECORD, data);
    }


}