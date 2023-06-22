package no.nav.samordning.hendelser.hendelse

import org.hibernate.sql.results.internal.TupleImpl
import org.postgresql.core.Tuple
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
        SELECT ROW_NUMBER() OVER(PARTITION BY TPNR = '1000' ORDER BY ID) as index, HENDELSE_DATA FROM HENDELSER as hendelse
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
    ): List<jakarta.persistence.Tuple>


}