package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Hendelse;

public class Mapper {

    public static FeedHendelse map(Hendelse h) {
        var fh = new FeedHendelse();
        fh.identifikator = h.getIdentifikator();
        fh.vedtakId = h.getVedtakId();
        fh.ytelsesType = h.getYtelsesType();
        fh.fom = h.getFom();
        fh.tom = h.getTom();
        return fh;
    }
}