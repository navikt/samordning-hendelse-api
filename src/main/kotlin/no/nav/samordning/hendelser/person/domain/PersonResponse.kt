package no.nav.samordning.hendelser.person.domain

import java.time.LocalDate

data class PersonResponse (
    var sekvensnummer: Long,
    val tpnr: String,
    val fnr: String,
    val fnrGammelt: String?,
    val sivilstand: String?,
    val sivilstandDato: LocalDate?,
    val doedsdato: LocalDate?,
    val meldingskode: Meldingskode,
)
