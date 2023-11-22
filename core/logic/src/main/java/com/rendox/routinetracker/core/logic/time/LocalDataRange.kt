package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.random.Random

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

fun generateRandomDateRange(minDate: LocalDate, maxDate: LocalDate): LocalDateRange {
    val maxNumOfDays = minDate.daysUntil(maxDate) + 1
    val firstNumber = Random.nextInt(until = maxNumOfDays)
    val secondNumber = Random.nextInt(until = maxNumOfDays)
    val periodStartDate: LocalDate
    val periodEndDate: LocalDate
    if (firstNumber < secondNumber) {
        periodStartDate = minDate.plusDays(firstNumber)
        periodEndDate = minDate.plusDays(secondNumber)
    } else {
        periodStartDate = minDate.plusDays(secondNumber)
        periodEndDate = minDate.plusDays(firstNumber)
    }
    return periodStartDate..periodEndDate
}