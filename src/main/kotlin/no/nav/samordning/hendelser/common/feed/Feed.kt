package no.nav.samordning.hendelser.common.feed

import org.springframework.data.domain.Page
import java.net.URI

data class Feed<T: SequentialDTO>(var hendelser: List<T>, var sisteSekvensnummer: Long, var sisteLesteSekvensnummer: Long, var nextUrl: URI?) {

    constructor(page: Page<T>, queryURL: URI?) : this(
        page.content,
        page.totalElements,
        page.maxOfOrNull { it.sekvensnummer } ?: 0,
        queryURL
    )
}
