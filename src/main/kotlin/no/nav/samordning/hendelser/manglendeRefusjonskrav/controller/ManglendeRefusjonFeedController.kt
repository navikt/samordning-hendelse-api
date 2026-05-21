package no.nav.samordning.hendelser.manglendeRefusjonskrav.controller

import io.micrometer.core.annotation.Timed
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import no.nav.pensjonsamhandling.maskinporten.validation.annotation.Maskinporten
import no.nav.samordning.hendelser.common.feed.Feed
import no.nav.samordning.hendelser.common.feed.FeedBuilder
import no.nav.samordning.hendelser.common.security.TpConfigOrgNoValidator
import no.nav.samordning.hendelser.common.security.support.SCOPE_SAMORDNING
import no.nav.samordning.hendelser.manglendeRefusjonskrav.domain.ManglendeRefusjonskravResponse
import no.nav.samordning.hendelser.manglendeRefusjonskrav.service.ManglendeRefusjonskravService
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class ManglendeRefusjonFeedController(
    private val service: ManglendeRefusjonskravService,
    private val feedBuilder: FeedBuilder,
) {

    val log = LoggerFactory.getLogger(javaClass)!!

    @Timed
    @Valid
    @GetMapping(MANGLENDE_REFUSJONSKRAV_PATH)
    @Maskinporten(SCOPE_SAMORDNING, TpConfigOrgNoValidator::class)
    fun manglendeRefusjonskravFeed(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Int,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Int,
        @RequestParam(value = "sekvensnummer", required = false) @Min(1) sekvensnummer: Long?
    ): Feed<ManglendeRefusjonskravResponse> {
        val manglendeRefusjonskravHendelser = service.fetchSeqAndManglendeRefusjonskravHendelser(tpnr, sekvensnummer, side, antall)
        log.debug("tpnr = $tpnr, manglendeRefusjonskravHendelser.size = ${manglendeRefusjonskravHendelser.size}")
        return feedBuilder.forPath(MANGLENDE_REFUSJONSKRAV_PATH)
            .withTpnr(tpnr)
            .withSekvensnummer(sekvensnummer)
            .build(manglendeRefusjonskravHendelser)
    }

    companion object {
        const val MANGLENDE_REFUSJONSKRAV_PATH = "/hendelser/manglendeRefusjonskrav"
    }
}
