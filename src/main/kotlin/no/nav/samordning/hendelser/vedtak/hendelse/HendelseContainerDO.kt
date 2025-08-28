package no.nav.samordning.hendelser.vedtak.hendelse

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.vedtak.kafka.SamHendelse
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes.JSON

@Entity
@Table(name = "HENDELSER")
class HendelseContainerDO(
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    val tpnr: String,
    @JdbcTypeCode(JSON)
    @Column(name = "HENDELSE_DATA")
    val hendelseData: HendelseDO
) {
    constructor(samHendelse: SamHendelse) : this(
        id = 0L, tpnr = samHendelse.tpNr,
        hendelseData = HendelseDO(samHendelse)
    )
}