package no.nav.samordning.hendelser.vedtak.hendelse

import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import java.io.Serializable

@Suppress("unused")
class HendelseDO(
) : Serializable {

    constructor(hendelse: SamHendelse) : this(
    )
}
