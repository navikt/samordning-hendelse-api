package no.nav.samordning.hendelser.opprett;

import io.micrometer.core.annotation.Timed;
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

    @Timed(value = "post.counter.requests")
    @RequestMapping(path = "hendelser", method = RequestMethod.POST)
    public void nyHendelse(@RequestBody OpprettHendelseRequest hendelse) throws Exception {
        var domene = new Hendelse();
        domene.setVedtakId(hendelse.getVedtakId());
        domene.setFom(hendelse.getFom());
        domene.setTom(hendelse.getTom());
        domene.setIdentifikator(hendelse.getIdentifikator());
        domene.setYtelsesType(hendelse.getYtelsesType());
        database.insert(domene);
    }

}
