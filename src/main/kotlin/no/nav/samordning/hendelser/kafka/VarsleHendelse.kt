package no.nav.samordning.hendelser.kafka

import no.nav.samhandling.tp.domain.codestable.HendelseTypeCode

data class VarsleHendelse(
    val fnr: String,
    val hendeleType: HendelseTypeCode
)
