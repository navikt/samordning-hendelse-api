package no.nav.samordning.hendelser.ytelse.repository

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.ytelse.LocalDateTimeAttributeConverter
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseTypeCode
import java.time.LocalDateTime

@Entity
@Table(name = "YTELSE_HENDELSER")
data class YtelseHendelse(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    @Column(name = "SEKVENSNUMMER", nullable = false)
    var sekvensnummer: Long = 0,
    @Column(name = "TPNR", nullable = false)
    val tpnr: String,
    @Column(name = "FNR", nullable = false)
    val fnr: String,
    @Column(name = "HENDELSE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    val hendelseType: HendelseTypeCode,
    @Column(name = "YTELSE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    val ytelseType: YtelseTypeCode,
    @Column(name = "DATO_BRUK_FOM", nullable = false)
    @Convert(converter = LocalDateTimeAttributeConverter::class)
    val datoBrukFom: LocalDateTime,
    @Column(name = "DATO_BRUK_TOM", nullable = true)
    @Convert(converter = LocalDateTimeAttributeConverter::class)
    val datoBrukTom: LocalDateTime?
)
