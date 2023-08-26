package com.rendox.routinetracker.logic

import com.rendox.routinetracker.api.WeekDayRelativeToMonth
import kotlinx.datetime.LocalDate

sealed interface Schedule {
    fun isDue(validationDate: LocalDate): Boolean
}

object EveryDaySchedule : Schedule {
    override fun isDue(validationDate: LocalDate) = true
}

data class WeeklySchedule(var weekDaysWhenDue: List<Int>) : Schedule {
    override fun isDue(validationDate: LocalDate) =
        weekDaysWhenDue.contains(validationDate.dayOfWeek.value)
}

data class MonthlyTraditionalSchedule(var monthDaysWhenDue: List<Int>) : Schedule {

    override fun isDue(validationDate: LocalDate) =
        monthDaysWhenDue.contains(validationDate.dayOfMonth)
}

data class MonthlyCustomSchedule(val weekdaysToWeekNumbers: List<WeekDayMonthRelatedPattern>) :
    Schedule {
    override fun isDue(validationDate: LocalDate): Boolean {
        weekdaysToWeekNumbers.forEach {
            if (it.matches(validationDate)) return true
        }
        return false
    }
}

data class WeekDayMonthRelatedPattern(
    val weekDayIndex: Int,
    val weekDayRelativeToMonth: WeekDayRelativeToMonth,
) {
    fun matches(validationDate: LocalDate): Boolean {
        if (validationDate.dayOfWeek.value != weekDayIndex) return false

        return with(WeekDayRelativeToMonth) {
            val validationValue = deriveExplicitFrom(getNumber(validationDate))
            val pattern = if (weekDayRelativeToMonth != WeekDayRelativeToMonth.Last) {
                weekDayRelativeToMonth
            } else {
                val lastWeekDayNumber = getNumber( findDateOfLastWeekDayInMonth(validationDate))
                deriveExplicitFrom(lastWeekDayNumber)
            }

            validationValue == pattern
        }
    }
}

infix fun Int.to(that: WeekDayRelativeToMonth) =
    WeekDayMonthRelatedPattern(this, that)

//data class CustomPeriodSchedule(
//    val activityDaysNumber: Int = 1,
//    val restDaysNumber: Int,
//)
//
//data class SpecificDaysSchedule(
//    customDates: List<Date>,
//    repeatAnnually: Boolean = false,
//)


