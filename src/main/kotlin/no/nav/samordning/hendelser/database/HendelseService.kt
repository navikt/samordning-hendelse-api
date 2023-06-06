package no.nav.samordning.hendelser.database

import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.hendelse.HendelseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class HendelseService(val hendelseRepository: HendelseRepository, val databaseConfig: DatabaseConfig) {

    companion object {
        private val LOG = LoggerFactory.getLogger(HendelseService::class.java)
    }

    val totalHendelser: Long
        get() = hendelseRepository.count()

    fun getNumberOfPages(tpnr: String, sekvensnummer: Int, antall: Int) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toInt()
    } catch (e: Exception) {
        LOG.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        hendelseRepository.countAllByTpnrAndHendelseData_YtelsesTypeIn(tpnr, databaseConfig.ytelsesTyper.toSet())
    } catch (e: Exception) {
        LOG.warn(e.message)
        1L
    }

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Int, side: Int, antall: Int): Map<Long, Hendelse> {
        val offset = sekvensnummer.coerceAtLeast(1) + (side * antall) - 1
        return hendelseRepository.findAllByTpnrAndHendelseData_YtelsesTypeIn(
            tpnr, databaseConfig.ytelsesTyper.toSet(), OffsetPageRequest(antall, offset)
        ).run {
            content.mapIndexed { seq, it ->
                offset + seq.toLong() to it.hendelseData
            }.toMap()
        }
    }
}