package no.nav.samordning.hendelser.vedtak.hendelse

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
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
}

