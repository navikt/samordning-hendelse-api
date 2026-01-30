package no.nav.samordning.hendelser.person.repository

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@IdClass(PersonHendelseID::class)
@Table(name = "PERSON_HENDELSE")
data class PersonHendelse(
    @Id
    @Column(name = "HENDELSE_ID", nullable = false)
    val hendelseId: String,

    @Id
    @Column(name = "MELDINGSKODE", nullable = false)
    val meldingskode: String,

    @Column(name = "OPPRETTET_TIDSPUNKT", nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

@Embeddable
data class PersonHendelseID(val hendelseId: String, val meldingskode: String): Serializable
