package no.nav.samordning.hendelser.vedtak.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VedtakHendelseRepository : JpaRepository<VedtakHendelse, Long> {

    fun findAllByTpnrOrderBySekvensnummerAsc(
        tpnr: String,
        pageable: Pageable
    ): Page<VedtakHendelse>
}
