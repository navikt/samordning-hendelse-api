package no.nav.samordning.hendelser.hendelse

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import no.nav.samordning.hendelser.kafka.SamHendelse
import java.io.Serializable
import java.time.LocalDate

@Suppress("unused")
class Hendelse(
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fom: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    var tom: LocalDate? = null
) : Serializable {

    constructor(hendelse: SamHendelse) : this(
        ytelsesType = hendelse.ytelsesType,
        identifikator = hendelse.identifikator,
        vedtakId = hendelse.vedtakId,
        samId = hendelse.samId,
        fom = hendelse.fom,
        tom = hendelse.tom
    )
}

