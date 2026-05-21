package no.nav.samordning.hendelser.vedtak.controller

import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
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
import no.nav.samordning.hendelser.vedtak.hendelse.VedtakHendelseDTO
import no.nav.samordning.hendelser.vedtak.service.HendelseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.MOVED_PERMANENTLY
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class VedtakFeedController(
    private val feedBuilder: FeedBuilder,
) {

    @Autowired
    private lateinit var hendelseService: HendelseService

    @Autowired
    private lateinit var metrics: AppMetrics

    @Timed
    @Valid
    @GetMapping(VEDTAK_HENDELSER_PATH)
    @Maskinporten(SCOPE_SAMORDNING, TpConfigOrgNoValidator::class)
    fun hendelser(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Int,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Int,
        @RequestParam(value = "sekvensnummer", required = false) @Min(1) sekvensnummer: Long?
    ): Feed<VedtakHendelseDTO> {
        val hendelser = hendelseService.fetchSeqAndHendelser(tpnr, sekvensnummer, side, antall)
        metrics.incHendelserLest(tpnr, hendelser.numberOfElements.toDouble())
        return feedBuilder.forPath(VEDTAK_HENDELSER_PATH)
            .withTpnr(tpnr)
            .withSekvensnummer(sekvensnummer)
            .build(hendelser)
    }

    @GetMapping("/hendelser")
    @ResponseStatus(MOVED_PERMANENTLY)
    fun redirect(request: HttpServletRequest) = ResponseEntity.status(MOVED_PERMANENTLY)
        .header(HttpHeaders.LOCATION, VEDTAK_HENDELSER_PATH + request.queryString)
        .build<Unit>()

    companion object {
        const val VEDTAK_HENDELSER_PATH = "/hendelser/vedtak"
    }
}
