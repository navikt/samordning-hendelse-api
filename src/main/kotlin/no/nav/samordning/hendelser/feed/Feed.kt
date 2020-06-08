package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.hendelse.Hendelse

class Feed(var hendelser: List<Hendelse>, var sisteSekvensnummer: Long, var sisteLesteSekvensnummer: Long, var nextUrl: String?)
