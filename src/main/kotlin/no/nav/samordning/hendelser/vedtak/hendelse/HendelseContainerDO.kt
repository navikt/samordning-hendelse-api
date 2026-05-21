package no.nav.samordning.hendelser.vedtak.hendelse

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import java.time.LocalDate

@Entity
@Table(name = "VEDTAK_HENDELSE")
class HendelseContainerDO(
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    val tpnr: String,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: LocalDate,
    var tom: LocalDate? = null
) {
    constructor(hendelse: SamHendelse) : this(
        id = 0L,
        tpnr = hendelse.tpNr,
        ytelsesType = hendelse.ytelsesType,
        identifikator = hendelse.identifikator,
        vedtakId = hendelse.vedtakId,
        samId = hendelse.samId,
        fom = LocalDate.parse(hendelse.fom),
        tom = hendelse.tom?.let { LocalDate.parse(it) }
    )
}
