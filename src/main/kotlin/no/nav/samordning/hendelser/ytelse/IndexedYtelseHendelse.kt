package no.nav.samordning.hendelser.ytelse

import no.nav.samordning.hendelser.ytelse.repository.YtelseHendelse

interface IndexedYtelseHendelse {
    var index: Long
    var ytelseHendelse: YtelseHendelse
}