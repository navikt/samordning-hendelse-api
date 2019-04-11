package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.hendelse.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;


@RestController
public class FeedController {

    private Database database;

    @Autowired
    public FeedController(Database database) {
        this.database = database;
    }

    @Value("${FEED_MAX_ANTALL}")
    private int MAX_ANTALL;

    @Value("${FEED_DEFAULT_SIDE}")
    private int DEFAULT_SIDE;

    @Value("${FEED_DEFAULT_ANTALL}")
    private int DEFAULT_ANTALL;

    @Timed(value = "get.counter.requests")
    @RequestMapping(path = "hendelser", method = RequestMethod.GET)
    public Feed alleHendelser(HttpServletRequest request,
                              @RequestParam(value = "antall", required = false) Integer antall,
                              @RequestParam(value = "side", required = false) Integer side) throws BadParameterException {
        if (side == null) side = DEFAULT_SIDE;
        if (antall == null) antall = DEFAULT_ANTALL;
        if (antall > MAX_ANTALL) {
            throw new BadParameterException("Man kan ikke be om flere enn " + MAX_ANTALL + " hendelser.");
        }

        var feed = new Feed();
        var hendelser = database.fetch(side, antall);
        feed.setHendelser(hendelser.stream().map(Mapper::map).collect(Collectors.toList()));

        if (side < database.getNumberOfPages(antall)) {
            feed.setNext_url(request.getRequestURL().toString() +
                String.format("?side=%d&andtall=%d", side + 1, antall));
        }

        return feed;
    }

    private static Integer convertToInt(String value, String label) throws BadParameterException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadParameterException("Parameteren " + label + " er ikke et gyldig tall");
        }
    }
}
