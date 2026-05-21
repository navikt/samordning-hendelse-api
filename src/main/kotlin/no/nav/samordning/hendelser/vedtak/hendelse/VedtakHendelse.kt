package no.nav.samordning.hendelser.vedtak.hendelse

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import no.nav.samordning.hendelser.common.feed.SequentialDO
import no.nav.samordning.hendelser.common.security.support.SEKVENSNUMMER_DEFINITION
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.datatype.jsr310.deser.LocalDateDeserializer
import tools.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate

@Entity
@Table(name = "VEDTAK_HENDELSE")
class VedtakHendelse(
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(columnDefinition = "SERIAL")
    @JsonIgnore
    val id: Long = 0,
    @JsonIgnore
    val tpnr: String,
    @Generated(event = [EventType.INSERT])
    @Column(
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = SEKVENSNUMMER_DEFINITION
    )
    override val sekvensnummer: Long = 0,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val fom: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    var tom: LocalDate? = null
): SequentialDO<VedtakHendelseDTO> {

    override fun toDTO() = VedtakHendelseDTO(
        sekvensnummer = sekvensnummer,
        ytelsesType = ytelsesType,
        identifikator = identifikator,
        vedtakId = vedtakId,
        samId = samId,
        fom = fom,
        tom = tom
    )
}
