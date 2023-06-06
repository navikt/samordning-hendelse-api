package no.nav.samordning.hendelser.hendelse

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLJsonbJdbcType

@Entity
@Table(name = "HENDELSER")
class HendelseContainer(
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    val tpnr: String,
    @JdbcType(PostgreSQLJsonbJdbcType::class)
    @Column(name = "HENDELSE_DATA",)
    val hendelseData: Hendelse
)