package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
public class FeedController {

    @Value("${FEED_MAX_ANTALL}")
    private int MAX_ANTALL;

    @Value("${FEED_DEFAULT_SIDE}")
    private int DEFAULT_SIDE;

    @Value("${FEED_DEFAULT_ANTALL}")
    private int DEFAULT_ANTALL;

    private Database database;

    @Autowired
    public FeedController(Database database) {
        this.database = database;
    }

    @RequestMapping
    @Timed(value = "get.counter.requests")
    public Feed hendelser(HttpServletRequest request,
                          @RequestParam(value = "antall", required = false) Integer antall,
                          @RequestParam(value = "side", required = false) Integer side) throws BadParameterException {
        if (side == null) side = DEFAULT_SIDE;
        if (antall == null) antall = DEFAULT_ANTALL;
        if (antall > MAX_ANTALL) {
            throw new BadParameterException("Man kan ikke be om flere enn " + MAX_ANTALL + " hendelser.");
        }

        var hendelser = new ArrayList<>(database.fetch(side, antall));
        String nextUrl = null;
        if (side < database.getNumberOfPages(antall) - 1) {
            nextUrl = request.getRequestURL().toString() + String.format("?side=%d&antall=%d", side + 1, antall);
        }

        return new Feed(hendelser, nextUrl);
    }
}
