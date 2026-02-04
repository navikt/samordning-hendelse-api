package no.nav.samordning.hendelser.vedtak.service

import no.nav.samordning.hendelser.vedtak.config.DatabaseConfig
import no.nav.samordning.hendelser.vedtak.hendelse.Hendelse
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepository
import no.nav.samordning.hendelser.vedtak.hendelse.YtelseType
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import kotlin.math.ceil

@Service
class HendelseService(
    private val hendelseRepository: HendelseRepository,
    private val databaseConfig: DatabaseConfig,
    private val objectMapper: ObjectMapper
) {

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
        ).associate { it.index to objectMapper.readValue<Hendelse>(it.hendelse) }

    fun getNumberOfPagesByYtelse(tpnr: String, ytelser: Set<YtelseType>, sekvensnummer: Int, antall: Int) = try {
        latestSekvensnummerByYtelse(tpnr, ytelser).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toInt()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummerByYtelse(tpnr: String, ytelser: Set<YtelseType>) = try {
        hendelseRepository.countAllByTpnrAndYtelsesType(tpnr, ytelser.names())
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }

    fun fetchSeqAndHendelserPerYtelse(
        tpnr: String,
        ytelser: Set<YtelseType>,
        sekvensnummer: Int,
        side: Int,
        antall: Int
    ) = if(ytelser.isEmpty()) {
        hendelseRepository.findAllByTpnr(
            tpnr,
            sekvensnummer.coerceAtLeast(1) + (side * antall) - 1,
            antall
        )
    } else {
        hendelseRepository.findAllByTpnrAndYtelsesType(
            tpnr,
            ytelser.names(),
            sekvensnummer.coerceAtLeast(1) + (side * antall) - 1,
            antall
        )
    }.associate { it.index to objectMapper.readValue<Hendelse>(it.hendelse) }

    private fun Set<YtelseType>.names() = map { it.name }.toSet()
}
