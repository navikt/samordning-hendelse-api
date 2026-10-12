package no.nav.samordning.hendelser.vedtak.service

import no.nav.samordning.hendelser.common.OffsetPageRequest
import no.nav.samordning.hendelser.vedtak.repository.VedtakHendelseRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
class VedtakHendelseService(
    private val vedtakHendelseRepository: VedtakHendelseRepository
) {

    private val log = getLogger(javaClass)
    val totalHendelser: Long
        get() = vedtakHendelseRepository.count()

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Long?, side: Int, antall: Int) =
        vedtakHendelseRepository.findAllByTpnr(
            tpnr,
            OffsetPageRequest(sekvensnummer?.minus(1)?.coerceAtLeast(0), side, antall)
        )
}
