package com.rendox.routinetracker.logic

import com.rendox.routinetracker.api.TimeInterval
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

sealed class Routine(
    var title: String,
    var description: String,
    val startDate: LocalDate,
    // var image
//    var progress: Float = 0f,
//    var completables: List<Completable>,
//    val target: Target,
//    val schedule: Schedule,
) : Completable {

    /**
     * A period, during which the specified amount of work should be done for the routine's plan
     * to be considered completed.
     */
    abstract val targetPeriod: DatePeriod

    /**
     * When the vacation mode gets activated, each day in the period is a holiday
     */
    var vacationPeriod: TimeInterval? = null

//    fun computeRoutineStatus(date: LocalDate): CompletableStatus {
//        val completionStatus = computeCompletionStatus(date)
//        if (completionStatus != null) return completionStatus
//        // if the routine is paused,
////        if () return CompletableStatus.NotDue
//    }

    abstract fun computePlanningStatus(date: LocalDate): PlanningStatus
    abstract fun computeCompletionStatus(date: LocalDate): CompletionStatus?

//    fun isOnVacation(date: LocalDate) = vacationPeriod?.contains(date) ?: false
}

//class YesNoRoutine(
//    title: String,
//    description: String,
//) : Routine(title, description) {
//    override val targetPeriod = DatePeriod(days = 1)
//    private var completionHistory = emptyList<Boolean>()
//    override fun computeStatus(validationDate: LocalDate): CompletableStatus {
//        val periodBetweenStartAndCurrent = validationDate - startDate
//        val currentDayNumber = periodBetweenStartAndCurrent.days + 1
//        val currentDateIsInHistory = completionHistory.lastIndex < currentDayNumber
//
////        return if (currentDateIsInHistory) {
////            if (completionHistory[currentDayNumber]) {
////                CompletionStatus.FullyCompleted
////            } else {
////
////            }
////        } else {
////            TODO
////        }
//
//    }
//}


/**
 * This class represents the amount of work user must do in the specified [period]
 * to complete a routine for that [period].
 */
sealed class Target(open val period: DatePeriod) {

    object YesNoTarget : Target(period = DatePeriod(days = 1))

    data class NumericValueTarget(
        val value: Number,
        val unit: String,
        override val period: DatePeriod,
    ) : Target(period)

    data class TimeTarget(
        val duration: DateTimePeriod,
        override val period: DatePeriod
    ) : Target(period)

    /**
     * @param completables List of tasks and subtasks associated with this [Routine].
     * @param numberOfCompletables The number of tasks the user must do to complete the routine
     * in the specified [period].
     */
    data class CompletableListTarget(
        val numberOfCompletables: Int,
        override val period: DatePeriod,
    ) : Target(period)
}

class Vacation {
    private var vacationHistory: MutableList<Pair<Instant, Instant>> = mutableListOf()
    private var currentVacationInterval: TimeInterval? = null

    fun startVacation(duration: DateTimePeriod? = null) {
        val timeZone = TimeZone.currentSystemDefault()
        val startTime = Clock.System.now().toLocalDateTime(timeZone)
        if (duration == null) {
            // create an infinite interval
            currentVacationInterval = TimeInterval(
                timeZone = timeZone,
                startTime = startTime,
                endTime = null,
            )
        } else {
            // create a finite interval
            val vacationInterval = TimeInterval(
                timeZone = timeZone,
                startTime = startTime,
                duration = duration,
            )
            currentVacationInterval = vacationInterval
            vacationHistory.add(vacationInterval.convertToInstants())
        }
    }

    /**
     * Should be called only for infinite vacations.
     */
    fun finishVacation() {
        vacationHistory.add(currentVacationInterval!!.convertToInstants())
        currentVacationInterval = null
    }

    fun isOnVacation(
        date: LocalDateTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): Boolean {
        if (currentVacationInterval == null) return false
        if (currentVacationInterval!!.contains(date, timeZone)) return true

        return false
    }
}
