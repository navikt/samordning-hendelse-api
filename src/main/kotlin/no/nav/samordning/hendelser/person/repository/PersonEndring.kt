package no.nav.samordning.hendelser.person.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.samordning.hendelser.person.domain.Meldingskode
import java.time.LocalDate

@Entity
@Table(name = "PERSON_ENDRING")
data class PersonEndring(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
    @Column(name = "SEKVENSNUMMER", nullable = false)
    var sekvensnummer: Long = 0,
    @Column(name = "TPNR", nullable = false)
    val tpnr: String,
    @Column(name = "FNR", nullable = false)
    val fnr: String,
    @Column(name = "FNR_GAMMELT")
    val fnrGammelt: String,
    @Column(name = "SIVILSTAND")
    val sivilstand: String,
    @Column(name = "SIVILSTAND_DATO")
    val sivilstandDato: LocalDate,
    @Column(name = "DOEDSDATO")
    val doedsdato: LocalDate,
    @Column(name = "MELDINGSKODE", nullable = false)
    @Enumerated(EnumType.STRING)
    val meldingskode: Meldingskode,
)