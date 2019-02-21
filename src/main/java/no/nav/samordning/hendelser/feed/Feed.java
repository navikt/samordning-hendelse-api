package no.nav.samordning.hendelser.feed;


import java.util.List;

public class Feed {
    private String next_url;

    private List<FeedHendelse> hendelser;

    public List<FeedHendelse> getHendelser() {
        return hendelser;
    }

    public void setHendelser(List<FeedHendelse> hendelser) {
        this.hendelser = hendelser;
    }

    public String getNext_url() {
        return next_url;
    }

    void setNext_url(String next_url) {
        this.next_url = next_url;
    }
}
