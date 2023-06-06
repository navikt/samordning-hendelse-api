package no.nav.samordning.hendelser.hendelse

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepository: JpaRepository<HendelseContainer, Long> {

    fun countAllByTpnrAndHendelseData_YtelsesTypeIn(tpnr: String, hendelseData_ytelsesType: Set<String>): Long

    fun findAllByTpnrAndHendelseData_YtelsesTypeIn(tpnr: String, hendelseData_ytelsesType: Set<String>, pageable: Pageable): Page<HendelseContainer>
}