package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.LocalDate

data class LocalDateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : Iterable<LocalDate>, ClosedRange<LocalDate> {

    override fun iterator(): Iterator<LocalDate> {
        return LocalDateIterator(start, endInclusive)
    }

    override fun toString() = "$start..$endInclusive"
}

class LocalDateIterator(
    start: LocalDate,
    private val endInclusive: LocalDate,
) : Iterator<LocalDate> {
    private var initValue = start
    override fun hasNext() =
        initValue <= endInclusive

    override fun next(): LocalDate {
        val result = initValue
        initValue = initValue.plusDays(1)
        return result
    }
}

operator fun LocalDate.rangeTo(that: LocalDate) = LocalDateRange(this, that)

fun LocalDateRange.isSubsetOf(other: LocalDateRange): Boolean =
    other.start <= start && endInclusive <= other.endInclusive