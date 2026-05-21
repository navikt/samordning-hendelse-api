package no.nav.samordning.hendelser.common

import org.springframework.data.domain.*
import java.util.*

class OffsetPageRequest(val preOffset: Long, private val pageRequest: PageRequest): Pageable by pageRequest {
    constructor(offset: Long?, page: Int, size: Int): this(offset ?: 0, PageRequest.of(page, size))

    override fun getOffset() = preOffset + pageRequest.offset

    override fun isPaged(): Boolean {
        return super.isPaged()
    }

    override fun isUnpaged(): Boolean {
        return super.isUnpaged()
    }

    override fun getSortOr(sort: Sort): Sort {
        return super.getSortOr(sort)
    }

    override fun toOptional(): Optional<Pageable> {
        return super.toOptional()
    }

    override fun toLimit(): Limit {
        return super.toLimit()
    }

    override fun toScrollPosition(): OffsetScrollPosition {
        return super.toScrollPosition()
    }
}
