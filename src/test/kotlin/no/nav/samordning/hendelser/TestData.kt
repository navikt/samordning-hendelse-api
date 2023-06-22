package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.hendelse.Hendelse
import no.nav.samordning.hendelser.hendelse.HendelseContainer
import java.time.LocalDate

object TestData {

    val h1000 = HendelseContainer(1, "1000", Hendelse("AAP", "01016600000", "1",null, LocalDate.of(2020, 1, 1), null))
    val h2000 = HendelseContainer(2, "2000", Hendelse("ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1)))
    val h3000 = HendelseContainer(3, "3000", Hendelse("ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1)))
    val h4000GP = HendelseContainer(4, "4000", Hendelse("GP", "01016800000", "3", null, LocalDate.of(2022, 1, 1), null))
    val h4000IP = HendelseContainer(5, "4000", Hendelse("IP", "01016900000", "4", null, LocalDate.of(2023, 1, 1), LocalDate.of(2033, 1, 1)))
    val h4000PT = HendelseContainer(6, "4000", Hendelse("PT", "01017000000", "5", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1)))
    val h5000 = HendelseContainer(7, "5000", Hendelse("NOT_IN_FILTER", "01018000000", "6", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1)))
}
