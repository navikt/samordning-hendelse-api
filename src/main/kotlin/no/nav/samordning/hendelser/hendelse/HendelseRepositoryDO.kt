package no.nav.samordning.hendelser.hendelse

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepositoryDO : JpaRepository<HendelseContainerDO, Long> {}
