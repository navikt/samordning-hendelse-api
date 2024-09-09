package no.nav.samordning.hendelser.ytelse.repository

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseTypeCode
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "YTELSE_HENDELSER")
data class YtelseHendelse(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    @JsonIgnore
    val id: Long,
    val fnr: String,
    val tpnr: String,
    @Column(name = "HENDELSE_TYPE", nullable = false)
    val hendelseType: HendelseTypeCode,
    @Column(name = "YTELSE_TYPE", nullable = false)
    val ytelseType: YtelseTypeCode,
    @Column(name = "DATO_BRUK_FOM", nullable = false)
    val datoFom: LocalDateTime,
    @Column(name = "DATO_FOM_MEDLEMSKAP", nullable = true)
    val datoFomMedlemskap: LocalDate?,
    @Column(name = "DATO_BRUK_TOM", nullable = true)
    val datoTom: LocalDateTime?
)
