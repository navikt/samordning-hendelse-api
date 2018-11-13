package no.nav.samordning.hendelser.opprett;

import no.nav.samordning.hendelser.Metrics;
import no.nav.samordning.hendelser.hendelse.Database;
import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class NyHendelseController {

    private Database database;

    @Autowired
    public NyHendelseController(Database database) {
        this.database = database;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void nyHendelse(@RequestBody OpprettHendelseRequest hendelse){
        Metrics.incPostRequests();
        var domene = new Hendelse();
        domene.setVedtakId(hendelse.getVedtakId());
        domene.setFom(hendelse.getFom());
        domene.setTom(hendelse.getTom());
        domene.setIdentifikator(hendelse.getIdentifikator());
        domene.setYtelsesType(hendelse.getYtelsesType());
        database.insert(domene);
    }

}
