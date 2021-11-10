package no.nav.samordning.hendelser.hendelse

import java.time.LocalDate

@NoArg
data class Hendelse(
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    val samId: String? = null,
    val fom: LocalDate,
    val tom: LocalDate? = null
)
