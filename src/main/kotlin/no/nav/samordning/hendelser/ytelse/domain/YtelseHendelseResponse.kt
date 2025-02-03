package no.nav.samordning.hendelser.ytelse.domain

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
class YtelseHendelseResponse(
    val sekvensnummer: Long,
    val tpnr: String,
    @JsonAlias("fnr")
    val identifikator: String,
    val hendelseType: HendelseTypeCode,
    @JsonAlias("tpArt")
    val ytelseType: String,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val datoFom: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val datoTom: LocalDate?
)
