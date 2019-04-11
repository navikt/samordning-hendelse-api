package no.nav.samordning.hendelser.hendelse;

import java.time.LocalDate;

public class Hendelse {

    private String ytelsesType;
    private String identifikator;
    private String vedtakId;
    private LocalDate fom;
    private LocalDate tom;

    public Hendelse(String identifikator, String ytelsesType, String vedtakId, String fom, String tom) {
        this.identifikator = identifikator;
        this.ytelsesType = ytelsesType;
        this.vedtakId = vedtakId;
        this.fom = LocalDate.parse(fom);
        this.tom = null;
        if (tom != null) this.tom = LocalDate.parse(tom);
    }

    public Hendelse() {}

    public String getYtelsesType() {
    return ytelsesType;
    }

    public void setYtelsesType(String ytelsesType) {
    this.ytelsesType = ytelsesType;
    }

    public String getIdentifikator() {
    return identifikator;
    }

    public void setIdentifikator(String identifikator) {
    this.identifikator = identifikator;
    }

    public String getVedtakId() {
    return vedtakId;
    }

    public void setVedtakId(String vedtakId) {
    this.vedtakId = vedtakId;
    }

    public LocalDate getFom() {
    return fom;
    }

    public void setFom(LocalDate fom) {
    this.fom = fom;
    }

    public LocalDate getTom() {
    return tom;
    }

    public void setTom(LocalDate tom) {
    this.tom = tom;
    }
}

