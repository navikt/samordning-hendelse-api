package no.nav.samordning.hendelser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
@RestController
@RequestMapping("hendelser")
public class Application {



	@Autowired
	private JdbcTemplate database;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@RequestMapping(method = RequestMethod.GET)
	public List<Hendelse> alleHendelser(){
		Object[] arguments = {};
		return database.query("Select ytelsesType, identifikator, vedtakId, fom, tom FROM T_SAMORDNINGSPLIKTIG_VEDTAK", BeanPropertyRowMapper.newInstance(Hendelse.class), arguments);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void nyHendelse(@RequestBody Hendelse hendelse){
		Date fom = Date.valueOf(hendelse.getFom());
		Date tom = hendelse.getTom() == null ? null : Date.valueOf(hendelse.getTom());
		database.update("insert into T_SAMORDNINGSPLIKTIG_VEDTAK(ytelsesType, identifikator, vedtakId, fom, tom) values(?,?,?, ?, ?)", hendelse.getYtelsesType(), hendelse.getIdentifikator(), hendelse.getVedtakId(), fom, tom);
	}



	public static class Hendelse{
		private String ytelsesType;
		private String identifikator;
		private String vedtakId;
		private LocalDate fom;
		private LocalDate tom;


		public String getYtelsesType() {
			return ytelsesType;
		}

		public void setYtelsesType(String ytelsesType) {
			this.ytelsesType = ytelsesType;
		}

		public String getIdentifikator() {
			return identifikator;
		}

		public void setIdentifikator(String identifikator) {
			this.identifikator = identifikator;
		}

		public String getVedtakId() {
			return vedtakId;
		}

		public void setVedtakId(String vedtakId) {
			this.vedtakId = vedtakId;
		}

		public LocalDate getFom() {
			return fom;
		}

		public void setFom(LocalDate fom) {
			this.fom = fom;
		}

		public LocalDate getTom() {
			return tom;
		}

		public void setTom(LocalDate tom) {
			this.tom = tom;
		}
	}


}
