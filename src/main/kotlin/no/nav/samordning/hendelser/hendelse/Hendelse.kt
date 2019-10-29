package no.nav.samordning.hendelser.hendelse;

public class Hendelse {

    private String ytelsesType;
    private String identifikator;
    private String vedtakId;
    private String fom;
    private String tom;

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

    public String getFom() {
    return fom;
    }

    public void setFom(String fom) {
    this.fom = fom;
    }

    public String getTom() {
    return tom;
    }

    public void setTom(String tom) {
    this.tom = tom;
    }
}

