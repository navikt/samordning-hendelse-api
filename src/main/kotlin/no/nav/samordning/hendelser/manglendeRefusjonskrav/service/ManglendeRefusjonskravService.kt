package no.nav.samordning.hendelser.manglendeRefusjonskrav.service

import no.nav.samordning.hendelser.manglendeRefusjonskrav.domain.ManglendeRefusjonskravResponse
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskravRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class ManglendeRefusjonskravService(
    private val manglendeRefusjonskravRepository: ManglendeRefusjonskravRepository,
) {

    private val log = getLogger(javaClass)

    fun getNumberOfPages(tpnr: String, sekvensnummer: Long, antall: Long) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toLong()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        manglendeRefusjonskravRepository.countAllByTpnr(tpnr)
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }


    fun fetchSeqAndManglendeRefusjonskravHendelser(tpnr: String, sekvensnummer: Long, side: Long, antall: Long): List<ManglendeRefusjonskravResponse> {
        val start = sekvensnummer.coerceAtLeast(1) + (side * antall)
        return manglendeRefusjonskravRepository.findByTpnrAndSekvensnummerBetween(
            tpnr,
            start,
            start + antall
        ).map { entity -> ManglendeRefusjonskravResponse(
            entity.sekvensnummer,
            entity.tpnr,
            entity.fnr,
            entity.samId,
            entity.svarfrist,
        )}.toList()
    }


}