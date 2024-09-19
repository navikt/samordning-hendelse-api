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

    fun getFirstByTpnrOrderBySekvensnummerDesc(tpnr: String): YtelseHendelse?

    @Override
    fun saveAndFlush(ytelseHendelse: YtelseHendelse) : YtelseHendelse {
        val sekvensnummer = getFirstByTpnrOrderBySekvensnummerDesc(ytelseHendelse.tpnr)
            ?.sekvensnummer ?: 0
        ytelseHendelse.sekvensnummer = sekvensnummer + 1
        val result = this.save(ytelseHendelse)
        this.flush()
        return result
    }

}