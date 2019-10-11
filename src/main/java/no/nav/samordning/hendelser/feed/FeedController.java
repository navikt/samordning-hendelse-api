package no.nav.samordning.hendelser.feed;

import io.micrometer.core.annotation.Timed;
import no.nav.samordning.hendelser.database.Database;
import no.nav.samordning.hendelser.metrics.AppMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.*;
import java.util.ArrayList;

@RestController
@Validated
public class FeedController {

    @Autowired
    private Database database;

    @Autowired
    private AppMetrics metrics;

    @Value("${NEXT_BASE_URL}")
    private String nextBaseUrl;

    @Timed
    @GetMapping(path = "/hendelser")
    public Feed hendelser(
        HttpServletRequest request,
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) String tpnr,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero Integer side,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) Integer antall,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) Integer sekvensnummer) {

        var hendelser = new ArrayList<>(database.fetchHendelser(tpnr, sekvensnummer, side, antall));

        metrics.incHendelserLest(tpnr, hendelser.size());

        return new Feed(hendelser, database.latestSekvensnummer(tpnr), nextUrl(tpnr, sekvensnummer, antall, side));
    }

    private String nextUrl(String tpnr, Integer sekvensnummer, Integer antall, Integer side) {
        if (side >= database.getNumberOfPages(tpnr, sekvensnummer, antall) - 1) return null;
        return nextBaseUrl + String.format("/hendelser?tpnr=%s&sekvensnummer=%d&antall=%d&side=%d",
                tpnr, sekvensnummer, antall, side + 1);
    }
}
