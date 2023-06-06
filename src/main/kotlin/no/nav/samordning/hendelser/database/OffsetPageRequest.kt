package no.nav.samordning.hendelser.database

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort


class OffsetPageRequest(private val pageSize: Int, private val offset: Int) : Pageable {
    private val sort: Sort = Sort.by(Sort.Direction.DESC, "id")

    init {
        require(pageSize >= 1) { "Limit must not be less than one!" }
        require(offset >= 0) { "Offset index must not be less than zero!" }
    }

    override fun getPageNumber() = offset / pageSize

    override fun getPageSize() = pageSize
    override fun getOffset() = offset.toLong()

    override fun getSort() = sort

    override fun next() =
        OffsetPageRequest(pageSize, offset + pageSize)

    private fun previous(): Pageable =
        if (hasPrevious()) OffsetPageRequest(pageSize, offset - pageSize) else this

    override fun previousOrFirst() = if (hasPrevious()) previous() else first()

    override fun first() = OffsetPageRequest(pageSize, 0)

    override fun withPage(pageNumber: Int): Pageable {
        TODO("Not yet implemented")
    }

    override fun hasPrevious() = offset > pageSize
}