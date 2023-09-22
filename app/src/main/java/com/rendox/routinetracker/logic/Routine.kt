//package com.rendox.routinetracker.logic

//import com.rendox.routinetracker.api.LocalDateRange
//import com.rendox.routinetracker.api.rangeTo
//import com.rendox.routinetracker.feature_routine.domain.model.Completable
//import com.rendox.routinetracker.feature_routine.domain.model.CompletableStatus
//import com.rendox.routinetracker.feature_routine.domain.model.HistoricalStatus
//import com.rendox.routinetracker.feature_routine.domain.model.PlanningStatus
//import kotlinx.datetime.DatePeriod
//import kotlinx.datetime.DateTimeUnit
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.daysUntil
//import kotlinx.datetime.minus
//import kotlinx.datetime.plus

/**
 * @param scheduleDeviation Represents the number of backlog days when is negative, and vice versa,
 * the number of days the user is ahead, when positive. Zero value means there's no deviation from
 * the schedule.
 */
//sealed class Routine(
//    var title: String,
//    var description: String? = null,
//    val startDate: LocalDate,
//    val schedule: Schedule,
//    var backlogEnabled: Boolean = true,
//    var periodSeparation: Boolean = false,
//    private var vacationStartDate: LocalDate? = null,
//    private var vacationEndDate: LocalDate? = null,
//    var completionHistory: List<CompletableStatus> = emptyList(),
//    // when add an entry to the history
//    // increment if the day was expected to be skipped but was completed,
//    // decrement when the user was supposed to complete the day, but didn't do that
//    // nullify at the end of the period, if the periodSeparation is enabled
//    protected val scheduleDeviation: Int = 0,
//    protected var roomForErrorDays: Int = 4,
//    var image
//    var progress: Float = 0f,
//    var completables: List<Completable>,
//    val target: Target,
//    private var vacationPeriod: ClosedRange
//) : Completable {
//
//    fun putOnVacation(
//        startDate: LocalDate,
//        endDate: LocalDate? = null,
//    ) {
//        vacationStartDate = startDate
//        endDate?.let { vacationEndDate = it }
//    }
//
//    fun endVacation() {
//        vacationStartDate = null
//        vacationEndDate = null
//    }
//
//    // TODO add computeStatus overload that receives an arbitrary date range and returns
//    //  a list of CompletableStatus
//
//    // TODO allow the user to choose when a day starts (pass a different argument to this method
//    //  depending on the time and the preferences)
//
//    fun computeStatus(validationDate: LocalDate): CompletableStatus {
//        IntRange
//        val validationDateIndex = startDate.daysUntil(validationDate)
//
//        // routine didn't exist at that time
//        if (validationDateIndex < 0) return PlanningStatus.Unknown
//
//        if (validationDateIndex in completionHistory.indices) {
//            return completionHistory[validationDateIndex]
//        }
//
//        if (isCurrentlyOnVacation(validationDate)) {
//            return HistoricalStatus.OnVacation
//        }
//
//        return computePlanningStatus(validationDate)
//    }
//
//    protected abstract fun computePlanningStatus(validationDate: LocalDate): PlanningStatus
//
//    private fun isCurrentlyOnVacation(validationDate: LocalDate): Boolean {
//        vacationStartDate?.let {
//            if (validationDate < it) return false
//        } ?: return false
//        vacationEndDate?.let {
//            return validationDate <= it
//        }
//        return true
//    }
//
//    /**
//     * Returns the range of indices of the [completionHistory] that correspond to the given
//     * [periodRange]. The [completionHistory] is guaranteed to contain the resulting indices.
//     * For example, if the [completionHistory] contains only a part of the current period, the last
//     * element of the range will be the last element of the [completionHistory], even though the
//     * resulting range's size will not be equal to the given [periodRange]'s size.
//     * Returns null if the history does not contain the given [periodRange].
//     */
//    protected fun periodInHistoryIndices(periodRange: LocalDateRange): IntRange? {
//        val startPeriodDateIndex = startDate.daysUntil(periodRange.start)
//        if (startPeriodDateIndex > completionHistory.lastIndex) return null
//        var endPeriodDateIndex = startDate.daysUntil(periodRange.endInclusive)
//        if (endPeriodDateIndex > completionHistory.lastIndex) {
//            endPeriodDateIndex = completionHistory.lastIndex
//        }
//        return startPeriodDateIndex..endPeriodDateIndex
//    }
//
//    protected fun periodRange(date: LocalDate): LocalDateRange {
//        check(date >= startDate) {
//            "The routine did not exist at the specified date"
//        }
//
//        val startPeriodDate: LocalDate
//        val endPeriodDate: LocalDate
//
//        // user may want to start their WeeklySchedule from a specific weekday and not from
//        // the week day of the startDate
//        if (schedule is WeeklySchedule && schedule.startDateDayOfWeek != null) {
//            var startDateIteration = date
//            while (startDateIteration.dayOfWeek != schedule.startDateDayOfWeek && startDateIteration != startDate) {
//                startDateIteration = date.minus(DateTimeUnit.DAY)
//            }
//            startPeriodDate = startDateIteration
//
//            // we should consider cases when the routine starts in the middle of the week, but
//            // in either case the period ends at the same weekday, so the following code is universal
//            val endDateDayOfWeekIndex = schedule.startDateDayOfWeek.value - 1
//            var endDateIteration = startPeriodDate
//            while (endDateIteration.dayOfWeek.value != endDateDayOfWeekIndex) {
//                endDateIteration = endDateIteration.plus(DateTimeUnit.DAY)
//            }
//            endPeriodDate = endDateIteration
//        } else {
//            val daysInPeriod = schedule.numOfDaysInPeriod(date)
//            val dateIndex = startDate.daysUntil(date)
//            var counter = dateIndex
//            while ((counter % daysInPeriod) != 0) {
//                counter--
//            }
//            startPeriodDate = startDate.plus(DatePeriod(days = counter))
//            endPeriodDate = startPeriodDate.plus(DatePeriod(days = daysInPeriod))
//        }
//
//        return startPeriodDate..endPeriodDate
//    }
//
//    protected fun computePreviousDays(currentDate: LocalDate): List<CompletableStatus> {
//        val newHistory = mutableListOf<CompletableStatus>()
//        val dayAfterLastDayInHistory = startDate
//            .plus(DatePeriod(days = completionHistory.lastIndex + 1))
//        val previousDaysWereNotAddedToHistory = dayAfterLastDayInHistory != currentDate
//        // The following situation happens when the user hasn't added any entries for some time so
//        // the missing entries should be calculated and saved.
//        if (previousDaysWereNotAddedToHistory) {
//            for (date in dayAfterLastDayInHistory..currentDate) {
//                var status = computeStatus(date)
//                when(status) {
//                    PlanningStatus.Planned -> status = HistoricalStatus.NotCompleted
//                    PlanningStatus.NotDue -> status = HistoricalStatus.Skipped
//                }
////                if (status is HistoricalStatus) {
//                    newHistory.add(status)
////                } else {
////                    throw IllegalArgumentException("Status must be historical")
////                }
//            }
//        }
//        return newHistory
//    }
//}
//
//class YesNoRoutine(
//    title: String,
//    description: String,
//    startDate: LocalDate,
//    schedule: Schedule,
//    backlogEnabled: Boolean = true,
//    periodSeparation: Boolean = false,
//    scheduleDeviation: Int = 0,
//    roomForErrorDays: Int = 4,
//) : Routine(
//    title = title,
//    description = description,
//    startDate = startDate,
//    schedule = schedule,
//    backlogEnabled = backlogEnabled,
//    periodSeparation = periodSeparation,
//    scheduleDeviation = scheduleDeviation,
//    roomForErrorDays = roomForErrorDays,
//) {
//    override fun computePlanningStatus(validationDate: LocalDate): PlanningStatus {
//        val periodRange = periodRange(validationDate)
//        val periodIndices = periodInHistoryIndices(periodRange)
//
//        // TODO add support for schedule deviation
//
//        var daysToBeDoneCounter = 0
//        for (currentDate in periodRange) {
//            if (schedule.isDue(currentDate)) daysToBeDoneCounter++
//        }
//
//        var daysDoneCounter = 0
//        var considerBacklog = false
//
//        // Count completed days only if the current period is in the history. If it's not in the
//        // history at all, it means that neither of days is completed.
//        periodIndices?.let {
//            daysDoneCounter = completionHistory.slice(it).count {status ->
//                status == HistoricalStatus.FullyCompleted
//            }
//            // Don't consider backlog for future dates because otherwise all the future date will
//            // have the Planned status. Whereas we expect the result to reflect routine's frequency.
//            considerBacklog = backlogEnabled
//        }
//
//        val daysDone = daysDoneCounter
//        val daysToBeDone = daysToBeDoneCounter
//
//        val fullyCompletedForPeriod = daysDone >= daysToBeDone
//
//        print("because considerBacklog = $considerBacklog")
//
//        return if (!fullyCompletedForPeriod && (schedule.isDue(validationDate) || considerBacklog)) {
//            PlanningStatus.Planned
//        } else {
//            PlanningStatus.NotDue
//        }
//    }
//
//    fun addEntryToHistory(date: LocalDate, status: HistoricalStatus) {
//        // Adding entries for future days is restricted so there is no need to check if date > today
//        // However, the user can complete a task from a future date. In this case this data will be
//        // saved on the behalf of the current date, schedule deviation value will be incremented, and
//        // the current day will have OverCompleted status.
//
//        val newHistory = mutableListOf<CompletableStatus>()
//        newHistory.addAll(completionHistory)
//        newHistory.addAll(computePreviousDays(date))
//        newHistory.add(status)
//        completionHistory = newHistory
//    }
//}
