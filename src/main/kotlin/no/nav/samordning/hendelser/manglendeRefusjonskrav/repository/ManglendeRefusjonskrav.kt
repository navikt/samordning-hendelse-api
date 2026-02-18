package no.nav.samordning.hendelser.manglendeRefusjonskrav.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "MANGLENDE_REFUSJONSKRAV")
data class ManglendeRefusjonskrav(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long = 0,
    @Column(name = "SEKVENSNUMMER", nullable = false)
    var sekvensnummer: Long = 0,
    @Column(name = "TPNR", nullable = false)
    val tpnr: String,
    @Column(name = "FNR", nullable = false)
    val fnr: String,
    @Column(name = "SAM_ID")
    val samId: String,
    @Column(name = "SVARFRIST")
    val svarfrist: LocalDate,
)