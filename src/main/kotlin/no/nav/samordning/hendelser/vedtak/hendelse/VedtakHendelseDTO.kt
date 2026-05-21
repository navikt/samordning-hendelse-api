package no.nav.samordning.hendelser.vedtak.hendelse

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.samordning.hendelser.common.feed.SequentialDTO
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate

data class VedtakHendelseDTO(
    override val sekvensnummer: Long,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    val fom: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    var tom: LocalDate? = null
): SequentialDTO
