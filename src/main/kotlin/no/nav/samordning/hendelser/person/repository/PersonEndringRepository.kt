package no.nav.samordning.hendelser.person.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PersonEndringRepository: JpaRepository<PersonEndring, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM PERSON_ENDRING WHERE TPNR = :tpnr",
        nativeQuery = true
    )
    fun countAllByTpnr(tpnr: String): Long

    fun findByTpnrAndSekvensnummerBetween(tpnr: String, offset: Long, limit: Long): List<PersonEndring>

}