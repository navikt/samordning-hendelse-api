package no.nav.samordning.hendelser.common.feed

interface SequentialDO<T: SequentialDTO> {
    val sekvensnummer: Long

    fun toDTO(): T
}
