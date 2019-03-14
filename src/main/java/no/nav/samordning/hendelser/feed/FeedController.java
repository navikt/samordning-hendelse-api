package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.hendelse.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.stream.Collectors;


@RestController
public class FeedController {
    private static final Integer MAX_ANTALL = 10000;
    private static final String MIN_DATE_LOCALDATE = "1814-05-17";
    private static final String MAX_DATE_LOCALDATE = "2100-01-01";
    private static final LocalDate MIN_DATE = LocalDate.parse(MIN_DATE_LOCALDATE);
    private static final LocalDate MAX_DATE = LocalDate.parse(MAX_DATE_LOCALDATE);

    private static final String DEFAULT_SIDE = "0";
    private static final String DEFAULT_ANTALL = "10000";

    private Database database;

    @Autowired
    public FeedController(Database database) {
        this.database = database;
    }

    @Timed(value = "get.counter.requests")
    @RequestMapping(path = "hendelser", method = RequestMethod.GET)
    public Feed alleHendelser(HttpServletRequest request,
                              @RequestParam(value="side", defaultValue=DEFAULT_SIDE) String sideInt,
                              @RequestParam(value="antall", defaultValue=DEFAULT_ANTALL) String antallInt)
            throws BadParameterException {

        var side = convertToInt(sideInt, "side");
        var antall = convertToInt(antallInt, "antall");

        if(antall>MAX_ANTALL) {
            throw new BadParameterException("Man kan ikke be om flere enn " + MAX_ANTALL + " hendelser.");
        }

        var feed = new Feed();
        var hendelser = database.fetch(side, antall);
        feed.setHendelser(hendelser.stream().map(Mapper::map).collect(Collectors.toList()));

        if (side < database.getNumberOfPages()) {
            String sideParam = request.getQueryString().split("&")[0];
            feed.setNext_url(request.getRequestURL().toString() + "?" +
                    request.getQueryString().replace(sideParam, "side=" + (side + 1)));
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
