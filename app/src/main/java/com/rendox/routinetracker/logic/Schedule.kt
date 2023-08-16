package com.rendox.routinetracker.logic

//import com.rendox.routinetracker.api.NumberOfDaysInWeek
//
//sealed class Schedule {
//
//    /**
//     * When the vacation mode gets activated, each day in the period is a holiday
//     */
//    var vacationPeriod: Period? = null
//
//    /**
//     * The maximum period during which the Completable may not be completed
//     * until the streak breaks
//     */
//    var maxAllowedGapDays: Int = NumberOfDaysInWeek
//    var completionHistory: MutableMap<LocalDate, Boolean> = mutableMapOf()
//    val completables =
//
//    abstract fun isWorkday(date: LocalDate): Boolean
//
//    fun isOnVacation(date: LocalDate) = vacationPeriod?.contains(date) ?: false
//}
//
//object DailySchedule : Schedule() {
//    override fun isWorkday(date: LocalDate) = !isOnVacation(date)
//}
//
//data class WeeklySchedule(var weekDaysIndices: List<Int>) : Schedule() {
//    fun createSchedule(weekDays: List<WeekDay>) =
//        WeeklySchedule(weekDays.map { it.index })
//
//    fun createSchedule(numberOfDays: Int) =
//        WeeklySchedule(List(numberOfDays) { index -> index })
//
//    override fun isWorkday(date: LocalDate): Boolean {
//        if (isOnVacation(date)) return false
//        return weekDaysIndices.contains(date.dayOfWeek.value)
//    }
//}
//
//data class MonthlyTraditionalSchedule(val monthDaysIndices: List<Int>) {
//    fun createSchedule(monthDaysIndices: List<Int>) =
//        MonthlyTraditionalSchedule(monthDaysIndices)
//
//    fun createSchedule(numberOfDays: Int) =
//        MonthlyTraditionalSchedule(List(numberOfDays) { index -> index })
//}
//
//data class MonthlyCustomSchedule(
//    val weekdayToWeekNumber: Map<WeekDay, MonthWeekNumber>
//)
//
//data class CustomPeriodSchedule(
//    val activityDaysNumber: Int = 1,
//    val restDaysNumber: Int,
//)
//
//data class SpecificDaysSchedule(
//    customDates: List<Date>,
//    repeatAnnually: Boolean = false,
//)


