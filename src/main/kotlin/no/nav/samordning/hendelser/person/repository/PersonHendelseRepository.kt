package no.nav.samordning.hendelser.person.repository

import no.nav.samordning.hendelser.person.domain.Meldingskode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonHendelseRepository: JpaRepository<PersonHendelse, Long>  {

    fun existsByHendelseIdAndMeldingskode(hendelseId: String, meldingskode: Meldingskode): Boolean

}