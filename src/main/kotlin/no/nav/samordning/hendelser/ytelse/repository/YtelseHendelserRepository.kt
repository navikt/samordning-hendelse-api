package no.nav.samordning.hendelser.ytelse.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface YtelseHendelserRepository: JpaRepository<YtelseHendelse, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM YTELSE_HENDELSER WHERE TPNR = :tpnr",
        nativeQuery = true
    )
    fun countAllByTpnr(tpnr: String): Long

    fun findByTpnrAndSekvensnummerBetween(tpnr: String, offset: Long, limit: Long): List<YtelseHendelse>

    //    fun getFirstByTpnrOrderBySekvensnummerDesc(tpnr: String): YtelseHendelse?

    @Query(
        value = "SELECT SEKVENSNUMMER FROM YTELSE_HENDELSER WHERE TPNR = :tpnr " +
                "ORDER BY SEKVENSNUMMER DESC " +
                "LIMIT 1",
        nativeQuery = true
    )
    fun hentSisteBrukteSekvenskummer(tpnr: String): Long

}