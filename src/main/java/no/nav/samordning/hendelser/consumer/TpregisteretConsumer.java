package no.nav.samordning.hendelser.consumer;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class TpregisteretConsumer {

    private final Integer CONNECT_TIMEOUT_MILLIS = 3000;
    private final Integer READ_TIMEOUT_MILLIS = 5000;

    @Value("${TPREGISTERET_URL}")
    private String tpregisteretUri;

    private WebClient webClient;

    public TpregisteretConsumer(WebClient.Builder webClientBuilder) {
        webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient())))
                .build();
    }

    public Boolean validateOrganisation(String orgno, String tpnr) {
        var httpStatus = webClient.get().uri(tpregisteretUri + "/organisation/{orgno}/tpnr/{tpnr}", orgno, tpnr)
                .exchange().block().statusCode();

        return httpStatus == HttpStatus.OK;
    }

    private TcpClient tcpClient() {
        return TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_MILLIS / 1000)));
    }
}
