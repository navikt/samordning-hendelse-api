package no.nav.samordning.hendelser.ytelse.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface YtelseHendelserRepository: JpaRepository<YtelseHendelse, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM HENDELSER WHERE TPNR = :tpnr",
        nativeQuery = true
    )
    fun countAllByTpnr(tpnr: String): Long

    @Query(
        value = """
        SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = :tpnr ORDER BY ID) as index, * as hendelse FROM YTELSE_HENDELSER
            WHERE TPNR = :tpnr
            OFFSET :offset
            LIMIT :limit
    """, nativeQuery = true
    )
    fun findAllByTpnr(tpnr: String, offset: Int, limit: Int): List<YtelseHendelse>
}