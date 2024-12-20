package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.hendelse.HendelseContainer
import no.nav.samordning.hendelser.ytelse.domain.HendelseTypeCode
import no.nav.samordning.hendelser.ytelse.domain.YtelseHendelseDTO
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {

    val h1000 = HendelseContainer(1, "1000", Hendelse("AAP", "01016600000", "1",null, LocalDate.of(2020, 1, 1), null))
    val h2000 = HendelseContainer(2, "2000", Hendelse("ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1)))
    val h3000 = HendelseContainer(3, "3000", Hendelse("ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1)))
    val h4000GP = HendelseContainer(4, "4000", Hendelse("GP", "01016800000", "3", null, LocalDate.of(2022, 1, 1), null))
    val h4000IP = HendelseContainer(5, "4000", Hendelse("IP", "01016900000", "4", null, LocalDate.of(2023, 1, 1), LocalDate.of(2033, 1, 1)))
    val h4000PT = HendelseContainer(6, "4000", Hendelse("PT", "01017000000", "5", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1)))
    val h5000 = HendelseContainer(7, "5000", Hendelse("NOT_IN_FILTER", "01018000000", "6", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1)))

    val hy3010 = YtelseHendelseDTO(1L, "3010", "01016600000", HendelseTypeCode.OPPRETT, "LIVSVARIG_AFP", LocalDateTime.of(2020, 1, 1, 12, 12, 12), LocalDateTime.of(2022, 1, 1, 13, 13, 13))

    val hy3400 = YtelseHendelseDTO(1L, "3400", "14087459777", HendelseTypeCode.OPPRETT, "ALDER", LocalDateTime.of(2024, 1, 1, 1, 1, 1), null)
    val hy3400_2 = YtelseHendelseDTO(2L, "3400", "14087453666", HendelseTypeCode.OPPRETT, "OMS", LocalDateTime.of(2024, 12, 20, 1, 1, 1), null)
    val hy3400_3 = YtelseHendelseDTO(3L, "3400", "14087453333", HendelseTypeCode.OPPRETT, "UFORE", LocalDateTime.of(2024, 12, 20, 1, 1, 1), null)
}
