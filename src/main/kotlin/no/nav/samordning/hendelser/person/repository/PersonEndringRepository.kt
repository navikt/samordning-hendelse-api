package no.nav.samordning.hendelser.person.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonEndringRepository: JpaRepository<PersonEndring, Long> {

    fun findByTpnr(tpnr: String, pageable: Pageable): Page<PersonEndring>

}
