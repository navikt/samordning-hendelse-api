package no.nav.samordning.hendelser.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
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
    private val mapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build() ).registerModule(JavaTimeModule())
    private val LOG: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${VEDTAK_HENDELSE_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        LOG.info("*** Innkommende VedtakHendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")
        LOG.debug("VedtakHendelse: $hendelse")

        val samHendelse: SamHendelse = try {
            mapper.readValue<SamHendelse>(hendelse)
        } catch (e: Exception) {
            acknowledgment.acknowledge()
            LOG.error("Feilet ved deserializering *** acket, melding: ${e.message}", e)
            return
        }

        try {
            hendelseRepository.saveAndFlush(HendelseContainer(samHendelse))
            acknowledgment.acknowledge()
            LOG.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            LOG.error("Feilet ved lagre VedtakHendelse, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }
    }
}