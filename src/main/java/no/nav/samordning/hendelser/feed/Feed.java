package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Hendelse;

import java.util.List;

public class Feed {

    private List<Hendelse> hendelser;
    private String next_url;

    public Feed(List<Hendelse> hendelser, String nextUrl) {
        this.hendelser = hendelser;
        this.next_url = nextUrl;
    }

    public List<Hendelse> getHendelser() {
        return hendelser;
    }

    public void setHendelser(List<Hendelse> hendelser) {
        this.hendelser = hendelser;
    }

    public String getNext_url() {
        return next_url;
    }

    public void setNext_url(String next_url) {
        this.next_url = next_url;
    }
}
