package no.nav.samordning.hendelser.ytelse.domain

enum class YtelseTypeCode(
    val isSamordningspliktig: Boolean
) {
    ALDER(true),
    UFORE(true),
    GJENLEVENDE(true),
    BARN(true),
    AFP(true),
    UKJENT(true),

    OPPSATT_BTO_PEN(true),
    SAERALDER(true),

    PAASLAGSPENSJON(false),
    OVERGANGSTILLEGG(false),
    BETINGET_TP(false),
    LIVSVARIG_AFP(false);

    companion object {
        val samordningsPliktige: List<YtelseTypeCode> = values().filter { it.isSamordningspliktig }

        val ikkeSamordningspliktige: List<YtelseTypeCode> = values().filterNot { it.isSamordningspliktig }

        // TODO: Legge inn alle ytelsene i array (afp-offentlig 2025)
        val afpOffentligLivsvarig = arrayOf(LIVSVARIG_AFP, BETINGET_TP)
    }
}