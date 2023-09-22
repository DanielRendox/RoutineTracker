package com.rendox.routinetracker

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// TODO replace with user preference
val DefaultDayStart = LocalTime(0, 0, 0)

val epoch = LocalDate(1970, 1, 1)

data class ZonedDateTime(
    private val dateTime: LocalDateTime,
    private val timeZone: TimeZone,
) {
    fun getDateTime(newTimeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
        refreshTime(dateTime, timeZone, newTimeZone)

    companion object {
        /**
         * Converts [dateTime] to a [newTimeZone] if necessary, and returns the result.
         */
        fun refreshTime(
            dateTime: LocalDateTime,
            timeZoneCorrespondingToDateTime: TimeZone,
            newTimeZone: TimeZone,
        ): LocalDateTime {
            if (timeZoneCorrespondingToDateTime == newTimeZone) return dateTime
            val initialTime = dateTime.toInstant(timeZoneCorrespondingToDateTime)
            return initialTime.toLocalDateTime(newTimeZone)
        }
    }
}

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