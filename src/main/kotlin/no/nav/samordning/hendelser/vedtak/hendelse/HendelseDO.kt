package no.nav.samordning.hendelser.vedtak.hendelse

import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import java.io.Serializable

@Suppress("unused")
class HendelseDO(
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: String,
    var tom: String? = null
) : Serializable {

    constructor(hendelse: SamHendelse) : this(
        ytelsesType = hendelse.ytelsesType,
        identifikator = hendelse.identifikator,
        vedtakId = hendelse.vedtakId,
        samId = hendelse.samId,
        fom = hendelse.fom,
        tom = hendelse.tom
    )
}

