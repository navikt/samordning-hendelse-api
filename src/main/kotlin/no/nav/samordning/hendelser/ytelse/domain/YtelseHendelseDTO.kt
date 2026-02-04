package no.nav.samordning.hendelser.ytelse.domain

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.datatype.jsr310.deser.LocalDateDeserializer
import tools.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate

class YtelseHendelseDTO(
    val sekvensnummer: Long = 0,
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
