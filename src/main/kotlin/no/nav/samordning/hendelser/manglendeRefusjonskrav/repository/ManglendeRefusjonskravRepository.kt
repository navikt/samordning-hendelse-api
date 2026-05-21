package no.nav.samordning.hendelser.manglendeRefusjonskrav.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ManglendeRefusjonskravRepository: JpaRepository<ManglendeRefusjonskrav, Long> {

    fun findByTpnr(tpnr: String, pageable: Pageable): Page<ManglendeRefusjonskrav>

    fun findBySamId(samId: String): ManglendeRefusjonskrav?

}
