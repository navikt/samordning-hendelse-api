package no.nav.samordning.hendelser.vedtak.service

import no.nav.samordning.hendelser.common.OffsetPageRequest
import no.nav.samordning.hendelser.vedtak.config.DatabaseConfig
import no.nav.samordning.hendelser.vedtak.hendelse.HendelseRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service

@Service
class HendelseService(
    private val hendelseRepository: HendelseRepository,
    private val databaseConfig: DatabaseConfig
) {

    private val log = getLogger(javaClass)
    val totalHendelser: Long
        get() = hendelseRepository.count()

    fun fetchSeqAndHendelser(tpnr: String, sekvensnummer: Long?, side: Int, antall: Int) =
        hendelseRepository.findAllByTpnrAndYtelsesTypeIn(
            tpnr,
            databaseConfig.ytelsesTyper.toSet(),
            OffsetPageRequest(sekvensnummer?.minus(1)?.coerceAtLeast(0), side, antall)
        )
}
