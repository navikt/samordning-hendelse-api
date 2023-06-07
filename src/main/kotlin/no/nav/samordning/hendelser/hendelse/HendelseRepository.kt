package no.nav.samordning.hendelser.hendelse

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepository : JpaRepository<HendelseContainer, Long> {

    @Query(
        value = """
        SELECT COUNT(*) FROM HENDELSER
            WHERE TPNR = ?1
            AND HENDELSE_DATA ->>'ytelsesType' in ?2
    """, nativeQuery = true
    )
    fun countAllByTpnrAndYtelsesType(tpnr: String, ytelsesTyper: Set<String>): Long

    @Query(
        value = """
        SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = '1000' ORDER BY ID) AS key, HENDELSE_DATA AS value FROM HENDELSER
            WHERE TPNR = ?1
            AND HENDELSE_DATA ->>'ytelsesType' in ?2
            OFFSET ?3
            LIMIT ?4
    """, nativeQuery = true
    )
    fun findAllByTpnrAndYtelsesType(
        tpnr: String,
        ytelsesTyper: Set<String>,
        offset: Int,
        limit: Int
    ): Map<Long, Hendelse>
}