package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;

@RestController
@Validated
public class FeedController {

    private Database database;

    @Autowired
    public FeedController(Database database) {
        this.database = database;
    }

    @RequestMapping
    @Timed(value = "get.counter.requests")
    public Feed hendelser(
        HttpServletRequest request,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero Integer side,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) Integer antall,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) Integer sekvensnummer) {
        var hendelser = new ArrayList<>(database.fetch(side, antall, sekvensnummer));
        String nextUrl = null;
        if (side < database.getNumberOfPages(antall) - 1) {
            nextUrl = request.getRequestURL().toString() + String.format("?side=%d&antall=%d", side + 1, antall);
        }
        return new Feed(hendelser, nextUrl);
    }
}
