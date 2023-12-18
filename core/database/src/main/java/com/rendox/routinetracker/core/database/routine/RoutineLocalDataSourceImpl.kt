package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.TransactionWithoutReturn
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.toDayOfWeek
import com.rendox.routinetracker.core.database.di.toInt
import com.rendox.routinetracker.core.database.routine.model.RoutineType
import com.rendox.routinetracker.core.database.routine.model.ScheduleType
import com.rendox.routinetracker.core.database.routine.model.toExternalModel
import com.rendox.routinetracker.core.database.schedule.GetCompletionTime
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime

class RoutineLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : RoutineLocalDataSource {

    override suspend fun getRoutineById(routineId: Long): Habit {
        return withContext(dispatcher) {
            db.routineEntityQueries.transactionWithResult {
                val schedule = getScheduleEntity(routineId).toExternalModel(
                    dueDatesProvider = { getDueDates(it) },
                    weekDaysMonthRelatedProvider = { getWeekDayMonthRelatedDays(it) }
                )
                getRoutineEntity(routineId).toExternalModel(schedule)
            }
        }
    }

    private fun getRoutineEntity(id: Long): RoutineEntity =
        db.routineEntityQueries.getRoutineById(id).executeAsOne()

    private fun getScheduleEntity(id: Long): ScheduleEntity =
        db.scheduleEntityQueries.getScheduleById(id).executeAsOne()

    private fun getDueDates(scheduleId: Long): List<Int> =
        db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList()

    private fun getWeekDayMonthRelatedDays(scheduleId: Long): List<WeekDayMonthRelated> =
        db.weekDayMonthRelatedEntityQueries
            .getWeekDayMonthRelatedDays(scheduleId)
            .executeAsList()
            .map { entity ->
                WeekDayMonthRelated(
                    dayOfWeek = entity.weekDayIndex.toDayOfWeek(),
                    weekDayNumberMonthRelated = entity.weekDayNumberMonthRelated,
                )
            }

    override suspend fun insertRoutine(habit: Habit) {
        return withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                when (habit) {
                    is Habit.YesNoHabit -> {
                        insertYesNoRoutine(habit)
                        insertSchedule(habit.schedule)
                    }
                }
            }
        }
    }

    @Suppress("UnusedReceiverParameter")
    private fun TransactionWithoutReturn.insertYesNoRoutine(habit: Habit.YesNoHabit) {
        db.routineEntityQueries.insertRoutine(
            id = habit.id,
            type = RoutineType.YesNoRoutine,
            name = habit.name,
            description = habit.description,
            sessionDurationMinutes = habit.sessionDurationMinutes,
            progress = habit.progress,
            defaultCompletionTimeHour = habit.defaultCompletionTime?.hour,
            defaultCompletionTimeMinute = habit.defaultCompletionTime?.minute,
        )
    }

    private fun insertSchedule(schedule: Schedule) {
        when (schedule) {
            is Schedule.EveryDaySchedule ->
                insertEveryDaySchedule(schedule)

            is Schedule.WeeklyScheduleByDueDaysOfWeek -> {
                insertWeeklyScheduleByDueDaysOfWeek(schedule)
                insertDueDates(schedule.dueDaysOfWeek.map { it.toInt() })
            }

            is Schedule.WeeklyScheduleByNumOfDueDays ->
                insertWeeklyScheduleByNumOfDueDays(schedule)

            is Schedule.MonthlyScheduleByDueDatesIndices -> {
                insertMonthlyScheduleByDueDatesIndices(schedule)
                val insertedScheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                insertDueDates(schedule.dueDatesIndices)
                insertWeekDaysMonthRelated(insertedScheduleId, schedule.weekDaysMonthRelated)
            }

            is Schedule.MonthlyScheduleByNumOfDueDays ->
                insertMonthlyScheduleByNumOfDueDays(
                    schedule
                )

            is Schedule.PeriodicCustomSchedule ->
                insertPeriodicCustomSchedule(schedule)

            is Schedule.CustomDateSchedule -> {
                insertCustomDateSchedule(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualScheduleByDueDates -> {
                insertAnnualScheduleByDueDates(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualScheduleByNumOfDueDays ->
                insertAnnualScheduleByNumOfDueDays(schedule)
        }
    }

    private fun insertEveryDaySchedule(schedule: Schedule.EveryDaySchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.EveryDaySchedule,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = null,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertWeeklyScheduleByDueDaysOfWeek(schedule: Schedule.WeeklyScheduleByDueDaysOfWeek) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklyScheduleByDueDaysOfWeek,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            startFromRoutineStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertWeeklyScheduleByNumOfDueDays(schedule: Schedule.WeeklyScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklyScheduleByNumOfDueDays,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            startFromRoutineStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertMonthlyScheduleByDueDatesIndices(schedule: Schedule.MonthlyScheduleByDueDatesIndices) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlyScheduleByDueDatesIndices,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = schedule.startFromRoutineStart,
            includeLastDayOfMonthInMonthlySchedule = schedule.includeLastDayOfMonth,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertMonthlyScheduleByNumOfDueDays(schedule: Schedule.MonthlyScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlyScheduleByNumOfDueDays,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = schedule.startFromRoutineStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertWeekDaysMonthRelated(scheduleId: Long, values: List<WeekDayMonthRelated>) {
        for (weekDayMonthRelated in values) {
            db.weekDayMonthRelatedEntityQueries.insertWeekDayMonthRelatedEntry(
                id = null,
                scheduleId = scheduleId,
                weekDayIndex = weekDayMonthRelated.dayOfWeek.toInt(),
                weekDayNumberMonthRelated = weekDayMonthRelated.weekDayNumberMonthRelated,
            )
        }
    }

    private fun insertPeriodicCustomSchedule(schedule: Schedule.PeriodicCustomSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.PeriodicCustomSchedule,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = schedule.numOfDaysInPeriod,
        )
    }

    private fun insertCustomDateSchedule(schedule: Schedule.CustomDateSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.CustomDateSchedule,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = null,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertAnnualScheduleByDueDates(schedule: Schedule.AnnualScheduleByDueDates) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualScheduleByDueDates,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = schedule.startFromRoutineStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertAnnualScheduleByNumOfDueDays(schedule: Schedule.AnnualScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualScheduleByNumOfDueDays,
            routineStartDate = schedule.startDate,
            routineEndDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            startDayOfWeekInWeeklySchedule = null,
            startFromRoutineStartInMonthlyAndAnnualSchedule = schedule.startFromRoutineStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInPeriodicCustomSchedule = null,
        )
    }

    private fun insertDueDates(dueDates: List<Int>) {
        val lastInsertScheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
        for (dueDate in dueDates) {
            db.dueDateEntityQueries.insertDueDate(
                id = null,
                scheduleId = lastInsertScheduleId,
                dueDateNumber = dueDate,
                completionTimeHour = null,
                completionTimeMinute = null,
            )
        }
    }

    override suspend fun getAllRoutines(): List<Habit> {
        return withContext(dispatcher) {
            db.routineEntityQueries.getAllRoutines()
                .executeAsList()
                .map { routineEntity ->
                    val schedule = getScheduleEntity(routineEntity.id).toExternalModel(
                        dueDatesProvider = { getDueDates(it) },
                        weekDaysMonthRelatedProvider = { getWeekDayMonthRelatedDays(it) }
                    )
                    routineEntity.toExternalModel(schedule)
                }
        }
    }

    override suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        withContext(dispatcher) {
            db.dueDateEntityQueries.updateCompletionTime(
                completionTimeHour = newTime.hour,
                completionTimeMinute = newTime.minute,
                scheduleId = routineId,
                dueDateNumber = dueDateNumber,
            )
        }
    }

    override suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return withContext(dispatcher) {
            db.dueDateEntityQueries
                .getCompletionTime(routineId, dueDateNumber)
                .executeAsOneOrNull()
                ?.toExternalModel()
        }
    }

    private fun GetCompletionTime.toExternalModel(): LocalTime? {
        return if (completionTimeHour != null && completionTimeMinute != null) {
            LocalTime(hour = completionTimeHour, minute = completionTimeMinute)
        } else null
    }
}