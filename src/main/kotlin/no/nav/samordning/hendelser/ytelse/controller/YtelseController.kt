package no.nav.samordning.hendelser.ytelse.controller

import io.micrometer.core.annotation.Timed
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import no.nav.pensjonsamhandling.maskinporten.validation.annotation.Maskinporten
import no.nav.samordning.hendelser.common.feed.Feed
import no.nav.samordning.hendelser.common.feed.FeedBuilder
import no.nav.samordning.hendelser.common.metrics.AppMetrics
import no.nav.samordning.hendelser.common.security.TpConfigOrgNoValidator
import no.nav.samordning.hendelser.common.security.support.SCOPE_SAMORDNING
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseResponse
import no.nav.samordning.hendelser.ytelse.service.YtelseService
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class YtelseController(
    private val service: YtelseService,
    private val metrics: AppMetrics,
    private val feedBuilder: FeedBuilder,
) {

    val log = LoggerFactory.getLogger(YtelseController::class.java)!!

    @Timed
    @Valid
    @GetMapping(YTELSE_HENDELSER_URL)
    @Maskinporten(SCOPE_SAMORDNING, TpConfigOrgNoValidator::class)
    fun hendelserTpYtelser(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Int,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Int,
        @RequestParam(value = "sekvensnummer", required = false) @Min(1) sekvensnummer: Long?
    ): Feed<YtelseHendelseResponse> {
        val ytelseHendelser = service.fetchSeqAndYtelseHendelser(tpnr, sekvensnummer, side, antall)
        log.debug("tpnr = $tpnr, ytelseHendelser.size = ${ytelseHendelser.numberOfElements}")
        metrics.incHendelserTpYtelserLest(tpnr, ytelseHendelser.numberOfElements.toDouble())
        return feedBuilder.forPath(YTELSE_HENDELSER_URL)
            .withTpnr(tpnr)
            .withSekvensnummer(sekvensnummer)
            .build(ytelseHendelser)
    }

    companion object {
        const val YTELSE_HENDELSER_URL = "/hendelser/ytelser"
    }
}
