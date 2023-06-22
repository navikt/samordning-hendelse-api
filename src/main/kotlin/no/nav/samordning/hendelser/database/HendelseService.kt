package no.nav.samordning.hendelser.database

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.hendelse.HendelseRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.ceil

@Service
class HendelseService(val hendelseRepository: HendelseRepository, val databaseConfig: DatabaseConfig) {

    private val log = getLogger(javaClass)
    private val objectMapper = ObjectMapper()
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
        ).associate {
            it.get(0) as Long to objectMapper.readValue(it.get(1) as String, JsonNode::class.java).let {
                Hendelse(
                    ytelsesType = it["ytelsesType"].asText(),
                    identifikator = it["identifikator"].asText(),
                    vedtakId = it["vedtakId"].asText(),
                    fom = "(\\d{4})-(\\d+)-(\\d+)".toRegex().find(it["fom"].asText())!!.groups.let {
                        LocalDate.of(it[1]!!.value.toInt(), it[2]!!.value.toInt(), it[3]!!.value.toInt())
                    },
                    tom = "(\\d{4})-(\\d+)-(\\d+)".toRegex().find(it["tom"].asText())?.groups?.let {
                        LocalDate.of(it[1]!!.value.toInt(), it[2]!!.value.toInt(), it[3]!!.value.toInt())
                    }
                )
            }
        }
}
