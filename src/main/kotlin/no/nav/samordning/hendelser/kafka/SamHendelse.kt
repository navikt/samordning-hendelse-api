package no.nav.samordning.hendelser.kafka

class SamHendelse(
    val tpNr: String,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: String,
    var tom: String? = null
)
