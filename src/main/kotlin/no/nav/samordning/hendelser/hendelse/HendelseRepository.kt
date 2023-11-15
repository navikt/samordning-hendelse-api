package no.nav.samordning.hendelser.hendelse

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface HendelseRepository : JpaRepository<HendelseContainer, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM HENDELSER WHERE TPNR = :tpnr AND HENDELSE_DATA ->> 'ytelsesType' IN :ytelsesTyper",
        nativeQuery = true
    )
    fun countAllByTpnrAndYtelsesType(tpnr: String, ytelsesTyper: Set<String>): Long

    @Query(
        value = """
        SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = :tpnr ORDER BY ID) as index, HENDELSE_DATA as hendelse FROM HENDELSER
            WHERE TPNR = :tpnr
            AND HENDELSE_DATA ->> 'ytelsesType' in :ytelsesTyper
            OFFSET :offset
            LIMIT :limit
    """, nativeQuery = true
    )
    fun findAllByTpnrAndYtelsesType(
        tpnr: String,
        ytelsesTyper: Set<String>,
        offset: Int,
        limit: Int
    ): List<IndexedHendelse>

    @Query(
        value = """
        SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = :tpnr ORDER BY ID) as index, HENDELSE_DATA as hendelse FROM HENDELSER
            WHERE TPNR = :tpnr
            OFFSET :offset
            LIMIT :limit
    """, nativeQuery = true
    )
    fun findAllByTpnr(
        tpnr: String,
        offset: Int,
        limit: Int
    ): List<IndexedHendelse>


}
