package no.nav.samordning.hendelser.person.service

import no.nav.samordning.hendelser.person.domain.PersonResponse
import no.nav.samordning.hendelser.person.repository.PersonEndringRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class PersonService(
    private val personHendelseRepository: PersonEndringRepository,
) {

    private val log = getLogger(javaClass)

    fun getNumberOfPages(tpnr: String, sekvensnummer: Long, antall: Long) = try {
        latestSekvensnummer(tpnr).minus(sekvensnummer - 1).div(antall.toDouble()).let(::ceil).toLong()
    } catch (e: Exception) {
        log.warn(e.message)
        0
    }

    fun latestSekvensnummer(tpnr: String) = try {
        personHendelseRepository.countAllByTpnr(tpnr)
    } catch (e: Exception) {
        log.warn(e.message)
        1L
    }


    fun fetchSeqAndPersonEndringHendelser(tpnr: String, sekvensnummer: Long, side: Long, antall: Long): List<PersonResponse> {
        val start = sekvensnummer.coerceAtLeast(1) + (side * antall)
        return personHendelseRepository.findByTpnrAndSekvensnummerBetween(
            tpnr,
            start,
            start + antall
        ).map { entity -> PersonResponse(
            entity.sekvensnummer,
            entity.tpnr,
            entity.fnr,
            entity.fnrGammelt,
            entity.sivilstand,
            entity.sivilstandDato,
            entity.doedsdato,
            entity.meldingskode
        )}.toList()
    }


}