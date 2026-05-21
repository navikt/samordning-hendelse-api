package no.nav.samordning.hendelser.common.feed

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class FeedBuilder(
    @Value($$"${NEXT_BASE_URL}")
    rootURL: String
) {
    private val baseUrl = UriComponentsBuilder.fromUriString(rootURL)

    fun forPath(path: String) = Builder(baseUrl.cloneBuilder().path(path))

    class Builder(private val baseUrl: UriComponentsBuilder) {
        fun withSekvensnummer(sekvensnummer: Long?): Builder {
            if (sekvensnummer != null) baseUrl.queryParam("sekvensnummer", sekvensnummer)
            return this
        }

        fun withTpnr(tpnr: String): Builder {
            baseUrl.queryParam("tpnr", tpnr)
            return this
        }

        fun <T: SequentialDTO> build(page: Page<out SequentialDO<T>>) = Feed(
            page.map(SequentialDO<T>::toDTO),
            if (page.hasNext()) baseUrl.queryParam("antall", page.size)
                .queryParam("side", page.number + 1)
                .build().toUri()
            else null,
        )
    }
}
