package no.nav.samordning.hendelser.manglendeRefusjonskrav.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.samordning.hendelser.manglendeRefusjonskrav.domain.Meldingskode
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskrav
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskravRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import kotlin.collections.map

@Service
@Transactional
class ManglendeRefusjonskravListener(
    private val manglendeRefusjonskravRepository: ManglendeRefusjonskravRepository,
    private val mapper: ObjectMapper,
)  {

    private val logger: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${MANGLENDE_REFUSJONSKRAV_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        logger.info("*** Innkommende ManglendeRefusjonskravHendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")

        val manglendeRefusjonskrav: ManglendeRefusjonskrav = try {
            logger.debug("hendelse json: $hendelse")
            MDC.put("X-Transaction-Id", mapper.readTree(hendelse)["hendelseId"].asText())
            val kafkaHendelse = mapper.readValue<ManglendeRefusjonskravKafkaHendelse>(hendelse)

            ManglendeRefusjonskrav(
                    tpnr = kafkaHendelse.tpNr,
                    fnr = kafkaHendelse.fnr,
                    samId = kafkaHendelse.samId,
                    svarfrist = kafkaHendelse.svarfrist,
                )

        } catch (e: Exception) {
            logger.error("Feilet ved deserializering, Acket, melding må sendes på nytt, melding: ${e.message}", e)
            return
        }

        try {
            val sisteSekvensnummer = manglendeRefusjonskravRepository.getFirstByTpnrOrderBySekvensnummerDesc(manglendeRefusjonskrav.tpnr) ?.sekvensnummer ?: 0
            manglendeRefusjonskrav.sekvensnummer = sisteSekvensnummer + 1
            manglendeRefusjonskravRepository.saveAndFlush(manglendeRefusjonskrav)

            acknowledgment.acknowledge()
            logger.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            logger.error("Feilet ved lagre ManglendeRefusjonskrav, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }

    }
}

data class ManglendeRefusjonskravKafkaHendelse(
    val hendelseId: String,
    val tpNr: String,
    val fnr: String,
    val samId: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val svarfrist: LocalDate,
)