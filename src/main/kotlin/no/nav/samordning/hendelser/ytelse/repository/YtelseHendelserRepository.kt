package no.nav.samordning.hendelser.ytelse.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface YtelseHendelserRepository: JpaRepository<YtelseHendelse, Long> {

    fun findByMottakerOrderBySekvensnummerAsc(mottaker: String, pageable: Pageable): Page<YtelseHendelse>
}
