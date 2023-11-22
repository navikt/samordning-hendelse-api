package no.nav.samordning.hendelser.kafka

import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.hendelse.HendelseContainer
import no.nav.samordning.hendelser.hendelse.HendelseRepository
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
    private val hendelseRepository: HendelseRepository
) {
    private val LOG: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${VEDTAK_HENDELSE_KAFKA_TOPIC}"])
    fun listener(hendelse: SamHendelse, cr: ConsumerRecord<String, SamHendelse>, acknowledgment: Acknowledgment) {
        LOG.info("*** Innkommende VedtakHendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")
        //if (LOG.isDebugEnabled) LOG.debug("VedtakHendelse: $hendelse")

        try {
            hendelseRepository.saveAndFlush(
                HendelseContainer(hendelse)
            )
            acknowledgment.acknowledge()
            LOG.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            LOG.error("Feilet ved lagre VedtakHendelse, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }
    }
}