package no.nav.samordning.hendelser.vedtak.domain

enum class YtelseType(val type: String) {
    OMS("Omstillingsstønad"),
    ALDER("Alderspensjon"),
    AFP("Avtalefestet pensjon"),
    AAP("Arbeidsavklaringspenger"),
}
