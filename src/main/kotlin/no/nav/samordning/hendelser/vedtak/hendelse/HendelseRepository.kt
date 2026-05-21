package no.nav.samordning.hendelser.vedtak.hendelse

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepository : JpaRepository<VedtakHendelse, Long> {

    fun findAllByTpnrAndYtelsesTypeIn(
        tpnr: String,
        ytelsesTyper: Set<String>,
        pageable: Pageable
    ): Page<VedtakHendelse>
}
