package no.nav.samordning.hendelser.manglendeRefusjonskrav.domain

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.samordning.hendelser.common.feed.SequentialDTO
import java.time.LocalDate

data class ManglendeRefusjonskravResponse (
    override val sekvensnummer: Long,
    val tpnr: String,
    val fnr: String,
    val samId: String?,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val svarfrist: LocalDate?,
): SequentialDTO
