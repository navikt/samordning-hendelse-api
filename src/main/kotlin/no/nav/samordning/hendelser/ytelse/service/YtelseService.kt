package no.nav.samordning.hendelser.ytelse.service

import no.nav.samordning.hendelser.common.OffsetPageRequest
import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelserRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
class YtelseService(
    private val ytelseHendelserRepository: YtelseHendelserRepository,
) {

    private val log = getLogger(javaClass)
    val totalHendelsertpYtelser: Long
        get() = ytelseHendelserRepository.count()


    fun fetchSeqAndYtelseHendelser(tpnr: String, sekvensnummer: Long?, side: Int, antall: Int) =
        ytelseHendelserRepository.findByMottaker(
            tpnr,
            OffsetPageRequest(sekvensnummer?.minus(1)?.coerceAtLeast(0), side, antall)
        )
    
}
