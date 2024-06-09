package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class ZonedDateTime(
    private val dateTime: LocalDateTime,
    private val timeZone: TimeZone,
) {
    fun getDateTime(newTimeZone: TimeZone): LocalDateTime =
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