package no.nav.samordning.hendelser.ytelse.repository

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.common.feed.SequentialDO
import no.nav.samordning.hendelser.common.security.support.SEKVENSNUMMER_DEFINITION
import no.nav.samordning.hendelser.ytelse.LocalDateTimeAttributeConverter
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseResponse
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.LocalDateTime

@Entity
@Table(name = "YTELSE_HENDELSER")
class YtelseHendelse(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    @Generated(event = [EventType.INSERT])
    @Column(
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = SEKVENSNUMMER_DEFINITION
    )
    override val sekvensnummer: Long = 0,
    @Column(name = "TPNR", nullable = false)
    val tpnr: String,
    @Column(name = "MOTTAKER", nullable = false)
    val mottaker: String,
    @Column(name = "IDENTIFIKATOR", nullable = false)
    val identifikator: String,
    @Column(name = "HENDELSE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    val hendelseType: HendelseTypeCode,
    @Column(name = "YTELSE_TYPE", nullable = false)
    val ytelseType: String,
    @Column(name = "DATO_BRUK_FOM", nullable = false)
    @Convert(converter = LocalDateTimeAttributeConverter::class)
    val datoBrukFom: LocalDateTime,
    @Column(name = "DATO_BRUK_TOM", nullable = true)
    @Convert(converter = LocalDateTimeAttributeConverter::class)
    val datoBrukTom: LocalDateTime?
): SequentialDO<YtelseHendelseResponse> {

    override fun toDTO() = YtelseHendelseResponse(
        sekvensnummer,
        tpnr,
        identifikator,
        hendelseType,
        ytelseType,
        datoBrukFom.toLocalDate(),
        datoBrukTom?.toLocalDate()
    )
}
