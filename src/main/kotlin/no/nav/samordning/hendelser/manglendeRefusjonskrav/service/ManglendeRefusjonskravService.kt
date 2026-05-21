package no.nav.samordning.hendelser.manglendeRefusjonskrav.service

import no.nav.samordning.hendelser.common.OffsetPageRequest
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskrav
import no.nav.samordning.hendelser.manglendeRefusjonskrav.repository.ManglendeRefusjonskravRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ManglendeRefusjonskravService(
    private val manglendeRefusjonskravRepository: ManglendeRefusjonskravRepository,
) {

    private val log = getLogger(javaClass)


    fun fetchSeqAndManglendeRefusjonskravHendelser(tpnr: String, sekvensnummer: Long?, side: Int, antall: Int): Page<ManglendeRefusjonskrav> {
        return manglendeRefusjonskravRepository.findByTpnr(
            tpnr,
            OffsetPageRequest(sekvensnummer?.minus(1)?.coerceAtLeast(0), side, antall)
        )
    }


}
