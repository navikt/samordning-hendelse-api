package no.nav.samordning.hendelser.ytelse.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.annotation.Timed
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import no.nav.pensjonsamhandling.maskinporten.validation.annotation.Maskinporten
import no.nav.samordning.hendelser.common.feed.Feed
import no.nav.samordning.hendelser.common.metrics.AppMetrics
import no.nav.samordning.hendelser.common.security.TpConfigOrgNoValidator
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseResponse
import no.nav.samordning.hendelser.ytelse.service.YtelseService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class YtelseController(
    private val service: YtelseService
) {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var metrics: AppMetrics

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    val log = LoggerFactory.getLogger(YtelseController::class.java)

    @Timed
    @Valid
    @GetMapping(path = ["/hendelser/ytelser"])
    @Maskinporten("nav:pensjon/v1/samordning", TpConfigOrgNoValidator::class)
    fun hendelserTpYtelser(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Long,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Long,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) sekvensnummer: Long
    ): Feed<YtelseHendelseResponse> {
        val ytelseHendelser = service.fetchSeqAndYtelseHendelser(tpnr, sekvensnummer, side, antall)
        val latestReadSNR = ytelseHendelser.maxOfOrNull { it.sekvensnummer } ?: 1
        log.debug("tpnr = $tpnr, ytelseHendelser.size = ${ytelseHendelser.size}")
        metrics.incHendelserTpYtelserLest(tpnr, ytelseHendelser.size.toDouble())

        return Feed(
            ytelseHendelser,
            service.latestSekvensnummer(tpnr),
            latestReadSNR,
            nextUrl(tpnr, sekvensnummer, antall, side)
        )

    }

    private fun nextUrl(tpnr: String, sekvensnummer: Long, antall: Long, side: Long) =
        if (service.getNumberOfPages(
                tpnr, sekvensnummer, antall
            ) > side + 1
        ) "$nextBaseUrl/hendelser/ytelser?tpnr=$tpnr&sekvensnummer=$sekvensnummer&antall=$antall&side=${side + 1}"
        else null

}