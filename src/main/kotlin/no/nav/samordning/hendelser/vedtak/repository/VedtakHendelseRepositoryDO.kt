package no.nav.samordning.hendelser.vedtak.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VedtakHendelseRepositoryDO : JpaRepository<VedtakHendelseDO, Long> {}
