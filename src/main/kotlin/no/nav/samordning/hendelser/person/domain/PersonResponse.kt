package no.nav.samordning.hendelser.person.domain

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.samordning.hendelser.common.feed.SequentialDTO
import java.time.LocalDate

data class PersonResponse (
    override val sekvensnummer: Long,
    val tpnr: String,
    val fnr: String,
    val fnrGammelt: String? = null,
    val sivilstand: String? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val sivilstandDato: LocalDate? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val doedsdato: LocalDate? = null,
    val adresse: Adresse? = null,
    val meldingskode: Meldingskode,
): SequentialDTO
