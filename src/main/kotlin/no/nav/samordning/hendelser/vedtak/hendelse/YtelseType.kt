package no.nav.samordning.hendelser.vedtak.hendelse

enum class YtelseType(val type: String) {
    OMS("Omstillingsstønad"),
    ALDER("Alderspensjon"),
    AFP("Avtalefestet pensjon"),
    AAP("Arbeidsavklaringspenger"),
}