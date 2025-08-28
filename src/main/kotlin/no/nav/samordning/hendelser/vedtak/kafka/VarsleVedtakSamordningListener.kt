package no.nav.samordning.hendelser.vedtak.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseContainerDO
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepositoryDO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VarsleVedtakSamordningListener(
    private val hendelseRepository: HendelseRepositoryDO
) {
    private val mapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule())
    private val LOG: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${VEDTAK_HENDELSE_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        LOG.info("*** Innkommende VedtakHendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")

        val samHendelse: SamHendelse = try {
            LOG.debug("hendelse json: $hendelse")
            mapper.readValue<SamHendelse>(hendelse)
        } catch (e: Exception) {
            acknowledgment.acknowledge()
            LOG.error("Feilet ved deserializering, Acket, melding må sendes på nytt, melding: ${e.message}", e)
            return
        }

        try {
            val container = HendelseContainerDO(samHendelse)
            val id = hendelseRepository.saveAndFlush(container).id
            val hendelse = container.hendelseData
            LOG.info("Lagrer $id med tpnr='${container.tpnr}' og Hendelse{identifikator='*****', vedtakId='${hendelse.vedtakId}', samId='${hendelse.samId}', ytelsesType='${hendelse.ytelsesType}', fom='${hendelse.fom}', tom='${hendelse.tom}'")
            acknowledgment.acknowledge()
            LOG.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            LOG.error("Feilet ved lagre VedtakHendelse, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }
    }

}