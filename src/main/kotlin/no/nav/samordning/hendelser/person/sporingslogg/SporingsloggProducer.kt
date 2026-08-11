package no.nav.samordning.hendelser.person.sporingslogg

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.samordning.hendelser.person.repository.PersonEndring
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.*

@Component
class SporingsloggProducer(private val kafkaTemplate: KafkaTemplate<String, String>,
           @param:Value($$"${SPRONGSLOGG_KAFKA_TOPIC}") private val topic: String,
           private val objectMapper: ObjectMapper) {

    private val logger: Logger = getLogger(javaClass)


    fun sendHendelse(personEndring: PersonEndring) {

        logger.debug("Send Personendring-Hendelse over kafka til Sporingslogg")
        val data = createLoggMelding(personEndring)
        kafkaTemplate.send(topic, data).get()
        logger.debug("Personendring-Hendelse sendt til Sporingslogg")
    }


    private fun createLoggMelding( personEndring: PersonEndring): String {
        val data = objectMapper.convertValue<String>(personEndring, String::class.java)
        return (LoggMelding(
            id = null,
            person = personEndring.fnr,
            mottaker = null, //?? kan denne være null??
            tema = "PEN",
            samtykkeToken = "", //ikke i bruk
            behandlingsGrunnlag = "GDPR Art. 6(1)e. ????",
            uthentingsTidspunkt = LocalDateTime.now(),
            leverteData = data,
            dataForespoersel = "Oppdatert persondata i nav",
            leverandoer = "" //ikke i bruk
        )).base64Encode().toJson()
    }

    private fun LoggMelding.toJson() : String = objectMapper.convertValue<String>(this, String::class.java)

    private fun LoggMelding.base64Encode() : LoggMelding = this.copy(leverteData = Base64.getEncoder().encodeToString(this.leverteData!!.toByteArray()))

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