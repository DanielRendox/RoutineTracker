package com.rendox.routinetracker.api

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
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

enum class WeekDayRelativeToMonth {
    First,
    Second,
    Third,
    Forth,
    Fifth,
    Last;

    companion object {
        fun deriveExplicitFrom(weekDayNumber: Int) = when (weekDayNumber) {
            1 -> First
            2 -> Second
            3 -> Third
            4 -> Forth
            5 -> Fifth
            else -> {
                throw IllegalArgumentException("Counter value should be in the range 1..5")
            }
        }

        fun getNumber(date: LocalDate): Int {
            val firstWeekDay = findDateOfFirstWeekDayInMonth(date)
            var resultingDate = firstWeekDay
            var counter = 1
            while (resultingDate != date) {
                resultingDate = resultingDate.plus(DateTimeUnit.WEEK)
                counter++
            }
            return counter
        }
        fun findDateOfLastWeekDayInMonth(
            date: LocalDate
        ): LocalDate {
            var lastWeekDay = date.atEndOfMonth
            while (lastWeekDay.dayOfWeek != date.dayOfWeek) {
                lastWeekDay = lastWeekDay.minus(DatePeriod(days = 1))
            }
            return lastWeekDay
        }
        fun findDateOfFirstWeekDayInMonth(
            date: LocalDate
        ): LocalDate {
            var firstWeekDay = date.withDayOfMonth(1)
            while (firstWeekDay.dayOfWeek != date.dayOfWeek) {
                firstWeekDay = firstWeekDay.plus(DatePeriod(days = 1))
            }
            return firstWeekDay
        }
    }
}

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

/**
 * A time interval is infinite when endTime == null
 */
data class TimeInterval(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    private val startTime: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone),
    private val endTime: LocalDateTime? = null, // if null, it's an infinite interval
) {
    /**
     * Always creates a finite interval.
     * Create a period from [startTime] to the day that is [duration] ahead.
     */
    constructor(
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        startTime: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone),
        duration: DateTimePeriod,
    ) : this(
        timeZone,
        startTime,
        startTime.toInstant(timeZone).plus(duration, timeZone).toLocalDateTime(timeZone)
    )

    fun getStartTime(newTimeZone: TimeZone = TimeZone.currentSystemDefault()) =
        ZonedDateTime.refreshTime(startTime, timeZone, newTimeZone)

    fun getEndTime(newTimeZone: TimeZone = TimeZone.currentSystemDefault()) =
        endTime?.let { ZonedDateTime.refreshTime(it, timeZone, newTimeZone) }

    /**
     * Checks whether the [timeProvided] is in the period within [startTime] and [endTime].
     */
    fun contains(
        timeProvided: LocalDateTime,
        timeZoneProvided: TimeZone = TimeZone.currentSystemDefault(),
    ): Boolean {
        val time = ZonedDateTime.refreshTime(timeProvided, timeZoneProvided, timeZone)
        return if (endTime != null) {
            time in (startTime..endTime)
        } else {
            time >= startTime
        }
    }

    /**
     * Converts this [TimeInterval] to a [Pair] of [Instant]s.
     *
     * @throws NullPointerException if the [endTime] is null and thus the [TimeInterval] cannot be
     * converted.
     */
    fun convertToInstants() =
        Pair(startTime.toInstant(timeZone), endTime!!.toInstant(timeZone))
}

/**
 * Returns this date with another day of month.
 *
 * @throws IllegalArgumentException if the resulting date is invalid or exceeds the platform-specific boundary.
 */
fun LocalDate.withDayOfMonth(dayOfMonth: Int) = LocalDate(this.year, this.month, dayOfMonth)

/**
 * The beginning of the next month.
 */
val LocalDate.nextMonth
    get() = withDayOfMonth(1).plus(1, DateTimeUnit.MONTH)
val LocalDate.atEndOfMonth get() = nextMonth - DatePeriod(days = 1)