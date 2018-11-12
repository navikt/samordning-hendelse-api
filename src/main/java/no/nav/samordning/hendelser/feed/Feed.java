package no.nav.samordning.hendelser.feed;


import java.util.List;

public class Feed {
    private List<FeedHendelse> hendelser;

    public List<FeedHendelse> getHendelser() {
        return hendelser;
    }

    public void setHendelser(List<FeedHendelse> hendelser) {
        this.hendelser = hendelser;
    }
}
