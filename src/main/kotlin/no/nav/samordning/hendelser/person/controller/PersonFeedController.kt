package no.nav.samordning.hendelser.person.controller

import io.micrometer.core.annotation.Timed
import jakarta.validation.Valid
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import no.nav.pensjonsamhandling.maskinporten.validation.annotation.Maskinporten
import no.nav.samordning.hendelser.common.feed.Feed
import no.nav.samordning.hendelser.common.security.TpConfigOrgNoValidator
import no.nav.samordning.hendelser.person.domain.PersonResponse
import no.nav.samordning.hendelser.person.service.PersonService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class PersonFeedController(
    private val service: PersonService
) {
    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    val log = LoggerFactory.getLogger(javaClass)

    @Timed
    @Valid
    @GetMapping(path = ["/hendelser/personer"])
    @Maskinporten("nav:pensjon/v1/samordning", TpConfigOrgNoValidator::class)
    fun personFeed(
        @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
        @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Long,
        @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Long,
        @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) sekvensnummer: Long
    ): Feed<PersonResponse> {
        val personEndringHendelser = service.fetchSeqAndPersonEndringHendelser(tpnr, sekvensnummer, side, antall)
        val sisteLesteSekvensNummer = personEndringHendelser.maxOfOrNull { it.sekvensnummer } ?: 1

        log.debug("tpnr = $tpnr, personHendelser.size = ${personEndringHendelser.size}")

        return Feed(
            personEndringHendelser,
            service.latestSekvensnummer(tpnr),
            sisteLesteSekvensNummer,
            nextUrl(tpnr, sekvensnummer, antall, side)
        )

    }

    private fun nextUrl(tpnr: String, sekvensnummer: Long, antall: Long, side: Long) =
        if (service.getNumberOfPages(
                tpnr, sekvensnummer, antall
            ) > side + 1
        ) "$nextBaseUrl/hendelser/personer?tpnr=$tpnr&sekvensnummer=$sekvensnummer&antall=$antall&side=${side + 1}"
        else null

}