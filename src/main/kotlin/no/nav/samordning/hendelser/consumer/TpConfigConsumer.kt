package no.nav.samordning.hendelser.consumer

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient

@Service
class TpConfigConsumer(@Value("\${TPCONFIG_URL}") private val tpConfigUri: String) {

    private val webClient = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient())))
            .build()

    fun validateOrganisation(orgno: String, tpnr: String): Boolean? =
            (webClient.get().uri("$tpConfigUri/organisation/validate/" + tpnr + "_" + orgno)
                    .exchange().block()!!
                    .statusCode().is2xxSuccessful())
                    .also { LOG.info("validateOrganisation status [$orgno, $tpnr]: $it") }

    private fun tcpClient(): TcpClient =
            TcpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                    .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT_MILLIS / 1000)) }

    companion object {
        private val LOG = LoggerFactory.getLogger(TpConfigConsumer::class.java)
        private const val READ_TIMEOUT_MILLIS = 5000
        private const val CONNECT_TIMEOUT_MILLIS = 3000
    }
}
