package no.nav.samordning.hendelser.manglendeRefusjonskrav.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.samordning.hendelser.common.feed.SequentialDO
import no.nav.samordning.hendelser.common.security.support.SEKVENSNUMMER_DEFINITION
import no.nav.samordning.hendelser.manglendeRefusjonskrav.domain.ManglendeRefusjonskravResponse
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.LocalDate

@Entity
@Table(name = "MANGLENDE_REFUSJONSKRAV")
class ManglendeRefusjonskrav(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long = 0,
    @Generated(event = [EventType.INSERT])
    @Column(
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = SEKVENSNUMMER_DEFINITION
    )
    override val sekvensnummer: Long = 0,
    @Column(name = "TPNR", nullable = false)
    val tpnr: String,
    @Column(name = "FNR", nullable = false)
    val fnr: String,
    @Column(name = "SAM_ID")
    val samId: String,
    @Column(name = "SVARFRIST")
    val svarfrist: LocalDate,
): SequentialDO<ManglendeRefusjonskravResponse> {

    override fun toDTO() = ManglendeRefusjonskravResponse(
        sekvensnummer,
        tpnr,
        fnr,
        samId,
        svarfrist
    )
}
