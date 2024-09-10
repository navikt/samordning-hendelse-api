package no.nav.samordning.hendelser.ytelse.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelse
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class YtelseService(
    private val ytelseHendelserRepository: YtelseHendelserRepository,
) {

    private val log = getLogger(javaClass)
    val totalHendelser: Long
        get() = ytelseHendelserRepository.count()

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toInt()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        ytelseHendelserRepository.countAllByTpnr(tpnr)
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }


    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Int, side: Int, antall: Int) =
        ytelseHendelserRepository.findAllByTpnr(
            tpnr,
            sekvensnummer.coerceAtLeast(1) + (side * antall) - 1,
            antall
        ).associate { it.index to it.ytelseHendelse }


}