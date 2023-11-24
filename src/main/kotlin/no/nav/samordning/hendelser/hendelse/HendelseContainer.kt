package no.nav.samordning.hendelser.hendelse

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.kafka.SamHendelse
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes.JSON

@Entity
@Table(name = "HENDELSER")
class HendelseContainer(
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    val tpnr: String,
    @JdbcTypeCode(JSON)
    @Column(name = "HENDELSE_DATA", columnDefinition = "jsonb")
    @Convert(converter = JpaConverterJson::class)
    val hendelseData: Hendelse
) {
    constructor(samHendelse: SamHendelse) : this(
        id = 0L, tpnr = samHendelse.tpNr,
        hendelseData = Hendelse(samHendelse)
    )
}