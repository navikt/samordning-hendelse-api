package no.nav.samordning.hendelser.ytelse.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime

class YtelseHendelseDTO(
    val sekvensnummer: Long,
    val tpnr: String,
    val fnr: String,
    val hendelseType: HendelseTypeCode,
    val ytelseType: YtelseTypeCode,
    @JsonFormat(pattern="yyyy-MM-ddTHH:mm:ss", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val datoFom: LocalDateTime,
    @JsonFormat(pattern="yyyy-MM-ddTHH:mm:ss", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val datoTom: LocalDateTime?,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val datoFomMedlemskap: LocalDate?,
)