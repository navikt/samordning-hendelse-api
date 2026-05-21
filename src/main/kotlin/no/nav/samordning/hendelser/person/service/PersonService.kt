package no.nav.samordning.hendelser.person.service

import no.nav.samordning.hendelser.common.OffsetPageRequest
import no.nav.samordning.hendelser.person.repository.PersonEndring
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val personEndringRepository: PersonEndringRepository,
) {

    private val log = getLogger(javaClass)

    fun fetchSeqAndPersonEndringHendelser(tpnr: String, sekvensnummer: Long?, side: Int, antall: Int): Page<PersonEndring> =
        personEndringRepository.findByTpnr(
            tpnr,
            OffsetPageRequest(sekvensnummer?.minus(1)?.coerceAtLeast(0), side, antall)
        )
}
