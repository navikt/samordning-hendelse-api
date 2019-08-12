package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Hendelse;

import java.util.List;

public class Feed {

    private List<Hendelse> hendelser;
    private String nextUrl;

    public Feed(List<Hendelse> hendelser, String nextUrl) {
        this.hendelser = hendelser;
        this.nextUrl = nextUrl;
    }

    public List<Hendelse> getHendelser() {
        return hendelser;
    }

    public void setHendelser(List<Hendelse> hendelser) {
        this.hendelser = hendelser;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }
}
