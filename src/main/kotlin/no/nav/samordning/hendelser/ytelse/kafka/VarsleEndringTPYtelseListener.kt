package no.nav.samordning.hendelser.ytelse.kafka

import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseDTO
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelse
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@Service
@Transactional
class VarsleEndringTPYtelseListener(
    private val ytelseHendelserRepository: YtelseHendelserRepository,
    private val mapper: ObjectMapper
)  {

    private val logger: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${YTELSE_HENDELSE_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        logger.info("*** Innkommende YtelseHendelse (VarsleEndringTPYtelse). Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")
        val hendelseType: HendelseTypeCode
        val tpnr: String
        val ytelseType: String

        val ytelseHendelser: List<YtelseHendelse> = try {
            logger.debug("hendelse json: $hendelse")
            MDC.put("X-Transaction-Id", mapper.readTree(hendelse)["id"].asString())

            val kafkaHendelse = mapper.readValue<YtelseHendelseDTO>(hendelse)
            hendelseType = kafkaHendelse.hendelseType
            tpnr = kafkaHendelse.tpnr
            ytelseType = kafkaHendelse.ytelseType

            mapper.readTree(hendelse)["mottakere"].asIterable().map { mottaker ->
                YtelseHendelse(
                    id = 0,
                    tpnr = kafkaHendelse.tpnr,
                    mottaker = mottaker.asString(),
                    identifikator = kafkaHendelse.identifikator,
                    hendelseType = kafkaHendelse.hendelseType,
                    ytelseType = kafkaHendelse.ytelseType,
                    datoBrukFom = kafkaHendelse.datoFom.atStartOfDay(),
                    datoBrukTom = kafkaHendelse.datoTom?.atStartOfDay(),
                )

            }

        } catch (e: Exception) {
            acknowledgment.acknowledge()
            logger.error("Feilet ved deserializering, Acket, melding må sendes på nytt, melding: ${e.message}", e)
            return
        }

        try {
            val mottakereLog = ytelseHendelserRepository.saveAllAndFlush(ytelseHendelser)
                .associate { it.mottaker to it.sekvensnummer }

            logger.info("Lagrer med hendelseType: ${hendelseType}, tpnr: ${tpnr}, mottaker-sekvensnummer: ${mottakereLog}, ytelseTypeCode: ${ytelseType}.")
            acknowledgment.acknowledge()
            logger.info("*** Acket melding ferdig")
        } catch (e: Exception) {
            logger.error("Feilet ved lagre varsleSamordning, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }
    }
}
