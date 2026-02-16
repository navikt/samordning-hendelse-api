package no.nav.samordning.hendelser.person.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PersonResponse (
    var sekvensnummer: Long,
    val tpnr: String,
    val fnr: String,
    val fnrGammelt: String?,
    val sivilstand: String?,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val sivilstandDato: LocalDate?,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val doedsdato: LocalDate?,
    val meldingskode: Meldingskode,
)
