package no.nav.samordning.hendelser.feed

class Feed<T>(var hendelser: List<T>, var sisteSekvensnummer: Long, var sisteLesteSekvensnummer: Long, var nextUrl: String?)
