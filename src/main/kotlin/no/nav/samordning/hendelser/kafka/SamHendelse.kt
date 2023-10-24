package no.nav.samordning.hendelser.kafka

import java.time.LocalDate

class SamHendelse(
    val tpNr: String,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: LocalDate,
    var tom: LocalDate? = null
)
