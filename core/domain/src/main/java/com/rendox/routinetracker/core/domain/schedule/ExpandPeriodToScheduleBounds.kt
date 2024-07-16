package com.rendox.routinetracker.core.domain.schedule

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate

fun LocalDateRange.expandPeriodToScheduleBounds(
    schedule: Schedule,
    getPeriodRange: (LocalDate) -> LocalDateRange,
): LocalDateRange {
    val scheduleEndDate = schedule.endDate
    require(this.start <= this.endInclusive)
    if (schedule.startDate > this.endInclusive) return this
    if (scheduleEndDate != null && this.start > scheduleEndDate) return this

    val minDate = maxOf(this.start, schedule.startDate)
    val maxDate = when {
        scheduleEndDate != null && scheduleEndDate < this.endInclusive -> scheduleEndDate
        else -> this.endInclusive
    }
    val leftBound = getPeriodRange(minDate).start
    val rightBound = getPeriodRange(maxDate).endInclusive
    return leftBound..rightBound
}