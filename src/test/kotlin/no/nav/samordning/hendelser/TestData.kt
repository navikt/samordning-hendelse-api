package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.vedtak.hendelse.VedtakHendelse
import java.time.LocalDate

object TestData {

    val h1000 = VedtakHendelse(1, "1000", 1,"AAP", "01016600000", "1",null, LocalDate.of(2020, 1, 1), null)
    val h2000 = VedtakHendelse(2, "2000", 1, "ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1))
    val h3000 = VedtakHendelse(3, "3000", 1, "ET", "01016700000", "2", null, LocalDate.of(2021, 1, 1), LocalDate.of(2030, 1, 1))
    val h4000GP = VedtakHendelse(4, "4000", 1, "GP", "01016800000", "3", null, LocalDate.of(2022, 1, 1), null)
    val h4000IP = VedtakHendelse(5, "4000", 2, "IP", "01016900000", "4", null, LocalDate.of(2023, 1, 1), LocalDate.of(2033, 1, 1))
    val h4000PT = VedtakHendelse(6, "4000", 3,"PT", "01017000000", "5", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1))
    val h5000 = VedtakHendelse(7, "5000", 1, "NOT_IN_FILTER", "01018000000", "6", null, LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1))
}
