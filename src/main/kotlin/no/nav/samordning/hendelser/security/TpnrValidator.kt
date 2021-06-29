package no.nav.samordning.hendelser.security

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import no.nav.pensjonsamhandling.maskinporten.validation.orgno.RequestAwareOrganisationValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import javax.servlet.http.HttpServletRequest

@Service
class TpnrValidator(webClientBuilder: WebClient.Builder) : RequestAwareOrganisationValidator {

    @Value("\${TPREGISTERET_URL}")
    lateinit var tpregisteretUri: String

    private fun tcpClient(): TcpClient =
        TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT_MILLIS / 1000)) }

    private val webClient = webClientBuilder
            .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient())))
            .build()

    override fun invoke(orgno: String, o: HttpServletRequest): Boolean {
        val tpnr = o.getParameter("tpnr").substringBefore('?')
        return webClient.get().uri("$tpregisteretUri/organisation")
            .header("orgNr", orgno)
            .header("tpId", tpnr)
            .retrieve()
            .toBodilessEntity()
            .block()!!
            .statusCode.is2xxSuccessful.also { LOG.info("validateOrganisation status [$orgno, $tpnr]: $it") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TpnrValidator::class.java)
        private const val READ_TIMEOUT_MILLIS = 5000
        private const val CONNECT_TIMEOUT_MILLIS = 3000
    }
}