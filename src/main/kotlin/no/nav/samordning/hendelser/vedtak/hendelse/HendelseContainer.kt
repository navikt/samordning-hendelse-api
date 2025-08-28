package no.nav.samordning.hendelser.vedtak.hendelse

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
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
)