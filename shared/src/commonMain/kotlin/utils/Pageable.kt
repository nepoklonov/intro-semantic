package utils

import kotlinx.serialization.Serializable

@Serializable
class Pageable private constructor(
    private val inner: PageableInner?
) {
    constructor(page: Int = 0, size: Int = 1) : this(PageableInner(page, size))

    companion object {
        val ALL = Pageable(null)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Pageable) return false
        return inner == other.inner
    }

    override fun hashCode(): Int {
        return inner?.hashCode() ?: 0
    }

    val size get() = inner?.size ?: Int.MAX_VALUE
    val page get() = inner?.page ?: 0
}

@Serializable
private data class PageableInner(
    val page: Int,
    val size: Int
)

operator fun Pageable.component1() = page
operator fun Pageable.component2() = size