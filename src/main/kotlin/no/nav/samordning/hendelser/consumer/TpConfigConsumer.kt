package no.nav.samordning.hendelser.consumer

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Service
class TpConfigConsumer(@Value("\${tpconfig.url}") private val tpConfigUri: String) {
    private val log = getLogger(javaClass)

    private val webClient = WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient())).build()

    fun validateOrganisation(orgno: String, tpnr: String): Boolean? =
            webClient.get().uri("$tpConfigUri/organisation/validate/" + tpnr + "_" + orgno)
                .retrieve().toBodilessEntity().block()!!
                .statusCode.is2xxSuccessful
                .also { log.info("validateOrganisation status [$orgno, $tpnr]: $it") }


    private fun httpClient(): HttpClient =
        HttpClient.create().proxyWithSystemProperties().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
            .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT_MILLIS / 1000)) }

    companion object {
        private const val READ_TIMEOUT_MILLIS = 5000
        private const val CONNECT_TIMEOUT_MILLIS = 3000
    }
}
