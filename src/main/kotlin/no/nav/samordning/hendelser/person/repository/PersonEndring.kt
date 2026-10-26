package no.nav.samordning.hendelser.person.repository

import jakarta.persistence.*
import no.nav.samordning.hendelser.common.feed.SequentialDO
import no.nav.samordning.hendelser.common.security.support.SEKVENSNUMMER_DEFINITION
import no.nav.samordning.hendelser.person.domain.Adresse
import no.nav.samordning.hendelser.person.domain.Meldingskode
import no.nav.samordning.hendelser.person.domain.PersonResponse
import org.hibernate.annotations.Generated
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.generator.EventType
import org.hibernate.type.SqlTypes.JSON
import java.time.LocalDate
import kotlin.jvm.Transient

@Entity
@Table(name = "PERSON_ENDRING")
class PersonEndring(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "SERIAL")
    val id: Long,
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
    @Column(name = "FNR_GAMMELT")
    val fnrGammelt: String?,
    @Column(name = "SIVILSTAND")
    val sivilstand: String?,
    @Column(name = "SIVILSTAND_DATO")
    val sivilstandDato: LocalDate?,
    @Column(name = "DOEDSDATO")
    val doedsdato: LocalDate?,
    @JdbcTypeCode(JSON)
    @Column(name = "ADRESSE")
    val adresse: Adresse?,
    @Column(name = "MELDINGSKODE", nullable = false)
    @Enumerated(EnumType.STRING)
    val meldingskode: Meldingskode,

    //brukt for lagre personHendelse lenger ned i kafkalisner
    @Transient
    val hendelseId: String
): SequentialDO<PersonResponse> {

    override fun toDTO() = PersonResponse(
        sekvensnummer,
        tpnr,
        fnr,
        fnrGammelt,
        sivilstand,
        sivilstandDato,
        doedsdato,
        adresse,
        meldingskode
    )
}
