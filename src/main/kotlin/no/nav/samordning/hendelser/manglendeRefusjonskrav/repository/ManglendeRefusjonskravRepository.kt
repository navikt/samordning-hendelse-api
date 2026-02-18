package no.nav.samordning.hendelser.manglendeRefusjonskrav.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ManglendeRefusjonskravRepository: JpaRepository<ManglendeRefusjonskrav, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM MANGLENDE_REFUSJONSKRAV WHERE TPNR = :tpnr",
        nativeQuery = true
    )
    fun countAllByTpnr(tpnr: String): Long

    fun findByTpnrAndSekvensnummerBetween(tpnr: String, offset: Long, limit: Long): List<ManglendeRefusjonskrav>

    fun getFirstByTpnrOrderBySekvensnummerDesc(tpnr: String): ManglendeRefusjonskrav?

}