package no.nav.samordning.hendelser.ytelse.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface YtelseHendelserRepository: JpaRepository<YtelseHendelse, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM YTELSE_HENDELSER WHERE MOTTAKER = :mottaker",
        nativeQuery = true
    )
    fun countAllByMottaker(mottaker: String): Long

    fun findByMottakerAndSekvensnummerBetween(mottaker: String, offset: Long, limit: Long): List<YtelseHendelse>

    fun getFirstByMottakerOrderBySekvensnummerDesc(mottaker: String): YtelseHendelse?

}