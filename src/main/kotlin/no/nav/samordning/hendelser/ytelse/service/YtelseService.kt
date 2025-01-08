package no.nav.samordning.hendelser.ytelse.service

import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseDTO
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class YtelseService(
    private val ytelseHendelserRepository: YtelseHendelserRepository,
) {

    private val log = getLogger(javaClass)
    val totalHendelsertpYtelser: Long
        get() = ytelseHendelserRepository.count()

    fun getNumberOfPages(tpnr: String, sekvensnummer: Long, antall: Long) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toLong()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        ytelseHendelserRepository.countAllByMottaker(tpnr)
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }


    fun fetchSeqAndYtelseHendelser(tpnr: String, sekvensnummer: Long, side: Long, antall: Long): List<YtelseHendelseDTO> {
        val start = sekvensnummer.coerceAtLeast(1) + (side * antall)
        return ytelseHendelserRepository.findByMottakerAndSekvensnummerBetween(
            tpnr, 
            start,
            start + antall
        ).map { entity -> YtelseHendelseDTO(
            entity.sekvensnummer,
            entity.tpnr,
            entity.identifikator,
            entity.hendelseType,
            entity.ytelseType,
            entity.datoBrukFom,
            entity.datoBrukTom)
        }.toList()
    }
    
}