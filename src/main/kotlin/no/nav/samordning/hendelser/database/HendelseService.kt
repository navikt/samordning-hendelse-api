package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.hendelse.HendelseRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class HendelseService(val hendelseRepository: HendelseRepository, val databaseConfig: DatabaseConfig) {

    private val log = getLogger(javaClass)

    val totalHendelser: Long
        get() = hendelseRepository.count()

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toInt()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        hendelseRepository.countAllByTpnrAndYtelsesType(tpnr, databaseConfig.ytelsesTyper.toSet())
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Int, side: Int, antall: Int) =
        hendelseRepository.findAllByTpnrAndYtelsesType(
            tpnr,
            databaseConfig.ytelsesTyper.toSet(),
            sekvensnummer.coerceAtLeast(1) + (side * antall) - 1,
            antall
        )
}
