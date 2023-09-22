//package com.rendox.routinetracker.logic

//import com.rendox.routinetracker.api.WeekDayRelativeToMonth
//import com.rendox.routinetracker.api.atEndOfMonth
//import kotlinx.datetime.DatePeriod
//import kotlinx.datetime.DateTimeUnit
//import kotlinx.datetime.DayOfWeek
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.daysUntil
//import kotlinx.datetime.plus

//sealed class Schedule() {
//    abstract fun isDue(validationDate: LocalDate): Boolean
//    abstract fun numOfDaysInPeriod(dateInPeriod: LocalDate): Int
//}
//
//object EveryDaySchedule : Schedule() {
//    override fun isDue(validationDate: LocalDate) = true
//    override fun numOfDaysInPeriod(dateInPeriod: LocalDate) = 1
//}
//
//data class WeeklySchedule(
//    var weekDaysWhenDue: List<Int>,
//    val startDateDayOfWeek: DayOfWeek? = null,
//) : Schedule() {
//    override fun isDue(validationDate: LocalDate) =
//        weekDaysWhenDue.contains(validationDate.dayOfWeek.value)
//
//    override fun numOfDaysInPeriod(dateInPeriod: LocalDate) = DateTimeUnit.WEEK.days
//}
//
//data class MonthlyTraditionalSchedule(
//    var monthDaysWhenDue: List<Int>,
//    var includeLastDayOfMonth: Boolean = false,
//) : Schedule() {
//    override fun isDue(validationDate: LocalDate): Boolean {
//        val isLastDayOfMonth = validationDate.atEndOfMonth == validationDate
//        if (isLastDayOfMonth && includeLastDayOfMonth) return true
//        return monthDaysWhenDue.contains(validationDate.dayOfMonth)
//    }
//
//    override fun numOfDaysInPeriod(dateInPeriod: LocalDate) = dateInPeriod.atEndOfMonth.dayOfMonth
//}
//
//data class MonthlyCustomSchedule(val weekdaysToWeekNumbers: List<WeekDayMonthRelatedPattern>) :
//    Schedule() {
//    override fun isDue(validationDate: LocalDate): Boolean {
//        weekdaysToWeekNumbers.forEach {
//            if (it.matches(validationDate)) return true
//        }
//        return false
//    }
//
//    override fun numOfDaysInPeriod(dateInPeriod: LocalDate): Int {
//        TODO("Not yet implemented")
//    }
//}
//
//data class PeriodicCustomSchedule(
//    val numOfDaysInPeriod: Int,
//    val daysWhenDue: List<Int>,
//)
//data class WeekDayMonthRelatedPattern(
//    val weekDayIndex: Int,
//    val weekDayRelativeToMonth: WeekDayRelativeToMonth,
//) {
//    fun matches(validationDate: LocalDate): Boolean {
//        if (validationDate.dayOfWeek.value != weekDayIndex) return false
//
//        return with(WeekDayRelativeToMonth) {
//            val validationValue = deriveExplicitFrom(getNumber(validationDate))
//            val pattern: WeekDayRelativeToMonth =
//                if (weekDayRelativeToMonth != WeekDayRelativeToMonth.Last) {
//                    weekDayRelativeToMonth
//                } else {
//                    val lastWeekDayNumber = getNumber(findDateOfLastWeekDayInMonth(validationDate))
//                    deriveExplicitFrom(lastWeekDayNumber)
//                }
//
//            validationValue == pattern
//        }
//    }
//}
//
//infix fun Int.to(that: WeekDayRelativeToMonth) =
//    WeekDayMonthRelatedPattern(this, that)

//data class CustomPeriodSchedule(
//    val activityDaysNumber: Int = 1,
//    val restDaysNumber: Int,
//)
//
//data class SpecificDaysSchedule(
//    customDates: List<Date>,
//    repeatAnnually: Boolean = false,
//)


