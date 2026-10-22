package no.nav.samordning.hendelser.person.sporingslogg

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.samordning.hendelser.common.consumer.TpConfigConsumer
import no.nav.samordning.hendelser.person.repository.PersonEndring
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDateTime
import java.util.*

@Component
class SporingsloggProducer(private val kafkaTemplate: KafkaTemplate<String, String>,
    @param:Value($$"${SPRONGSLOGG_KAFKA_TOPIC}") private val topic: String,
    private val tpconsumer: TpConfigConsumer) {

    private val mapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()


    private val logger: Logger = getLogger(javaClass)

    suspend fun sendHendelse(hendelseData: String, personEndring: PersonEndring, tpnrSet: Collection<String>) {
        tpnrSet.forEach { tpnr ->
            val mottaker = tpconsumer.hentOrdning(tpnr)?.orgNr // <-- hente Org.nr fra TP-API ut ifra TPNR
            if (mottaker.isNullOrBlank()) return@forEach
            try {
                val loggMelding = createLoggMelding(hendelseData, personEndring, mottaker)
                logger.info("Sender person endring over kafka til sporingslogg: Mottaker = $mottaker fra tpnr: $tpnr")
                logger.debug("behandlingsGrunnlag: ${loggMelding.behandlingsGrunnlag}")
                val payloadData = loggMelding.toEncodeData()
                kafkaTemplate.send(topic, payloadData)
            } catch (ex: Exception) {
                logger.error("Feil ved sending personendring-Hendelse", ex)
            }
        }
        logger.info("*** Person endring sendt til sporingslogg END ***")
    }

    private fun createLoggMelding( hendelseData: String, personEndring: PersonEndring, mottaker: String): LoggMelding {
        return (LoggMelding(
            id = null,
            person = personEndring.fnr,
            mottaker = mottaker,
            tema = "PEN",
            samtykkeToken = "", //ikke i bruk
            behandlingsGrunnlag = "GDPR Art. 6(1)e. Samordningsloven §27 første og andre ledd. GDPR Art. 9(2)b",
            uthentingsTidspunkt = LocalDateTime.now(),
            leverteData = hendelseData,
            dataForespoersel = "Oppdatert persondata i nav",
            leverandoer = "" //ikke i bruk
        ))
    }

    private fun LoggMelding.toEncodeData() = this.copy(leverteData = Base64.getEncoder().encodeToString(this.leverteData!!.toByteArray())).toJson()
    private fun LoggMelding.toJson() : String = mapper.writeValueAsString(this)

}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class LoggMelding(
    val id: String?,
    val person: String?,
    val mottaker: String?,
    val tema: String?,
    val behandlingsGrunnlag: String?,
    val uthentingsTidspunkt: LocalDateTime?,
    val leverteData: String?,
    val samtykkeToken: String?,
    val dataForespoersel: String?,
    val leverandoer: String?
) {


}