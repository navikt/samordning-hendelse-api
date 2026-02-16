package no.nav.samordning.hendelser.person.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.samordning.hendelser.person.repository.PersonEndring
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.repository.PersonHendelse
import no.nav.samordning.hendelser.person.repository.PersonHendelseRepository
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

@Service
@Transactional
class PersonEndringListener(
    private val personEndringRepository: PersonEndringRepository,
    private val personHendelseRepository: PersonHendelseRepository,
    private val mapper: ObjectMapper,
)  {

    private val logger: Logger = getLogger(javaClass)

    @KafkaListener(topics = ["\${PERSON_ENDRING_KAFKA_TOPIC}"])
    fun listener(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        logger.info("*** Innkommende PersonEndringHendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()}")

        val personEndringHendelser: List<PersonEndring> = try {
            logger.debug("hendelse json: $hendelse")
            MDC.put("X-Transaction-Id", mapper.readTree(hendelse)["hendelseId"].asText())
            val kafkaHendelse = mapper.readValue<PersonEndringKafkaHendelse>(hendelse)

            if (personHendelseRepository.existsByHendelseIdAndMeldingskode(kafkaHendelse.hendelseId, kafkaHendelse.meldingsKode) ) {
                logger.info("PersonEndringHendelse med hendelseId: ${kafkaHendelse.hendelseId} allerede lagret, ignorerer melding.")
                acknowledgment.acknowledge()
                return
            }
            mapper.readTree(hendelse)["tpNr"].asIterable().map { tpNr ->
                PersonEndring(
                    id = 0,
                    tpnr = tpNr.asText(),
                    fnr = kafkaHendelse.fnr,
                    fnrGammelt = kafkaHendelse.oldFnr,
                    sivilstand = kafkaHendelse.sivilstand,
                    sivilstandDato = kafkaHendelse.sivilstandDato,
                    doedsdato = kafkaHendelse.dodsdato,
                    meldingskode = kafkaHendelse.meldingsKode,
                    hendelseId = kafkaHendelse.hendelseId
                )
            }

        } catch (e: Exception) {
            logger.error("Feilet ved deserializering, Acket, melding må sendes på nytt, melding: ${e.message}", e)
            return
        }

        try {
            val personEndring = personEndringHendelser.map { personEndring ->
                val sisteSekvensnummer = personEndringRepository.getFirstByTpnrOrderBySekvensnummerDesc(personEndring.tpnr) ?.sekvensnummer ?: 0
                personEndring.sekvensnummer = sisteSekvensnummer + 1
                personEndringRepository.save(personEndring)
            }
            personEndringRepository.flush()

            val entity = personEndring.first()
            logger.info("Lagrer med meldingskode: ${entity}, tpnr: ${entity.tpnr}.")

            personHendelseRepository.saveAndFlush(PersonHendelse(entity.hendelseId, entity.meldingskode))
            logger.info("Lagrer med hendelseId: ${entity.hendelseId}, meldingskode: ${entity.meldingskode}.")

            acknowledgment.acknowledge()
            logger.info("*** Acket melding ferdig")

        } catch (e: Exception) {
            logger.error("Feilet ved lagre PersonEndring, melding: ${e.message}", e)
            Thread.sleep(3000L) //sleep 3sek..
            throw e
        }

    }
}

data class PersonEndringKafkaHendelse(
    val hendelseId: String,
    val tpNr: List<String>,
    val fnr: String,
    val oldFnr: String? = null,
    val sivilstand: String? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val sivilstandDato: LocalDate? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val dodsdato: LocalDate? = null,
    val meldingsKode: Meldingskode
)