package no.nav.samordning.hendelser.ytelse.domain

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class YtelseHendelseDTO(
    val sekvensnummer: Long,
    val tpnr: String,
    @JsonAlias("fnr")
    val identifikator: String,
    val hendelseType: HendelseTypeCode,
    @JsonAlias("tpArt")
    val ytelseType: String,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val datoFom: LocalDateTime,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val datoTom: LocalDateTime?
)