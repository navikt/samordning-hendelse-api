package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.database.Database;
import no.nav.samordning.hendelser.metrics.AppMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    private Database database;

    @Autowired
    private AppMetrics metrics;

    @Timed
    @GetMapping(path = "/hendelser")
    public Feed hendelser(
        HttpServletRequest request,
        @RequestParam(value = "tpnr") String tpnr,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero Integer side,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) Integer antall,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) Integer sekvensnummer) {

        var hendelser = new ArrayList<>(database.fetchHendelser(tpnr, sekvensnummer, side, antall));

        String nextUrl = null;
        if (side < database.getNumberOfPages(tpnr, antall) - 1)
            nextUrl = request.getRequestURL().toString() + String.format("?tpnr=%s&side=%d&antall=%d", tpnr, side + 1, antall);

        return new Feed(hendelser, nextUrl);
    }
}
