package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;


@RestController
public class FeedController {

    private Database database;

    @Autowired
    public FeedController(Database database) {
        this.database = database;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Feed alleHendelser(){
        var feed = new Feed();
        var domeneHendelser = database.fetchAll();
        feed.setHendelser(domeneHendelser.stream().map(Mapper::map).collect(Collectors.toList()));
        return feed;
    }

}
