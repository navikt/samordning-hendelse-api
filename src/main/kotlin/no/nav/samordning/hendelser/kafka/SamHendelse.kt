package no.nav.samordning.hendelser.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

class SamHendelse(
    val tpNr: String,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    val fom: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    var tom: LocalDate? = null
)
