package no.nav.samordning.hendelser.person.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PersonResponse (
    var sekvensnummer: Long,
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
)
