package no.nav.samordning.hendelser.manglendeRefusjonskrav.controller

import io.micrometer.core.annotation.Timed
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import no.nav.pensjonsamhandling.maskinporten.validation.annotation.Maskinporten
import no.nav.samordning.hendelser.common.feed.Feed
import no.nav.samordning.hendelser.common.security.TpConfigOrgNoValidator
import no.nav.samordning.hendelser.manglendeRefusjonskrav.domain.ManglendeRefusjonskravResponse
import no.nav.samordning.hendelser.manglendeRefusjonskrav.service.ManglendeRefusjonskravService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class ManglendeRefusjonFeedController(
    private val service: ManglendeRefusjonskravService
) {
    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    val log = LoggerFactory.getLogger(javaClass)

    @Timed
    @Valid
    @GetMapping(path = ["/hendelser/manglendeRefusjonskrav"])
    @Maskinporten("nav:pensjon/v1/samordning", TpConfigOrgNoValidator::class)
    fun manglendeRefusjonskravFeed(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Long,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Long,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) sekvensnummer: Long
    ): Feed<ManglendeRefusjonskravResponse> {
        val manglendeRefusjonskravHendelser = service.fetchSeqAndManglendeRefusjonskravHendelser(tpnr, sekvensnummer, side, antall)
        val sisteLesteSekvensNummer = manglendeRefusjonskravHendelser.maxOfOrNull { it.sekvensnummer } ?: 1

        log.debug("tpnr = $tpnr, manglendeRefusjonskravHendelser.size = ${manglendeRefusjonskravHendelser.size}")

        return Feed(
            manglendeRefusjonskravHendelser,
            service.latestSekvensnummer(tpnr),
            sisteLesteSekvensNummer,
            nextUrl(tpnr, sekvensnummer, antall, side)
        )

    }

    private fun nextUrl(tpnr: String, sekvensnummer: Long, antall: Long, side: Long) =
        if (service.getNumberOfPages(
                tpnr, sekvensnummer, antall
            ) > side + 1
        ) "$nextBaseUrl/hendelser/manglendeRefusjonskrav?tpnr=$tpnr&sekvensnummer=$sekvensnummer&antall=$antall&side=${side + 1}"
        else null

}