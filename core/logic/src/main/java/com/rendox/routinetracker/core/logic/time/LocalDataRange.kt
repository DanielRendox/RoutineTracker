package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class LocalDateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
) : Iterable<LocalDate>, ClosedRange<LocalDate> {
    val daysNumber
        get() = (endInclusive - start).days + 1

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
        initValue = initValue.plus(DatePeriod(days = 1))
        return result
    }
}

operator fun LocalDate.rangeTo(that: LocalDate) = LocalDateRange(this, that)