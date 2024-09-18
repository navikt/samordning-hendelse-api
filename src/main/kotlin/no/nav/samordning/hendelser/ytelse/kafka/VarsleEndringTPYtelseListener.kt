package no.nav.samordning.hendelser.ytelse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseDTO
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelse
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VarsleEndringTPYtelseListener(
    private val ytelseHendelserRepository: YtelseHendelserRepository
)  {

    private val mapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule())
    private val logger: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${YTELSE_HENDELSE_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        logger.info("*** Innkommende YtelseHendelse (VarsleEndringTPYtelse). Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")

        val ytelseHendelse: YtelseHendelse = try {
            logger.debug("hendelse json: $hendelse")
            val ytelseHendelseDTO = mapper.readValue<YtelseHendelseDTO>(hendelse)

            YtelseHendelse(
                id = 0,
                sekvensnummer = ytelseHendelserRepository.hentSisteBrukteSekvenskummer(ytelseHendelseDTO.tpnr)+1,
                tpnr = ytelseHendelseDTO.tpnr,
                fnr = ytelseHendelseDTO.fnr,
                hendelseType = ytelseHendelseDTO.hendelseType,
                ytelseType = ytelseHendelseDTO.ytelseType,
                datoBrukFom = ytelseHendelseDTO.datoFom,
                datoBrukTom = ytelseHendelseDTO.datoTom
            )

        } catch (e: Exception) {
            acknowledgment.acknowledge()
            logger.error("Feilet ved deserializering, Acket, melding må sendes på nytt, melding: ${e.message}", e)
            return
        }

        try {
            val entity = ytelseHendelserRepository.saveAndFlush(ytelseHendelse)

            logger.info("Lagrer med hendelseType: ${entity.hendelseType}, tpnr: ${entity.tpnr}, sekvensummer: ${entity.sekvensnummer}, ytelseTypeCode: ${entity.ytelseType}")
            acknowledgment.acknowledge()
            logger.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            logger.error("Feilet ved lagre varsleSamordning, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }
    }

//    private fun saveAndFlush(ytelseHendelse: YtelseHendelse) {
//        val sekvensnummer = ytelseHendelserRepository
//            .getFirstByTpnrOrderBySekvensnummerDesc(ytelseHendelse.tpnr)
//            ?.sekvensnummer ?: 0
//        ytelseHendelse.sekvensnummer = sekvensnummer + 1
//        ytelseHendelserRepository.saveAndFlush(ytelseHendelse)
//    }

}