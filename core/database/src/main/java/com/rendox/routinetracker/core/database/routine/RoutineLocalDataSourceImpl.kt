package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.TransactionWithoutReturn
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.routine.model.RoutineType
import com.rendox.routinetracker.core.database.routine.model.ScheduleType
import com.rendox.routinetracker.core.database.routine.model.toAnnualSchedule
import com.rendox.routinetracker.core.database.routine.model.toCustomDateSchedule
import com.rendox.routinetracker.core.database.routine.model.toEveryDaySchedule
import com.rendox.routinetracker.core.database.routine.model.toMonthlySchedule
import com.rendox.routinetracker.core.database.routine.model.toPeriodicCustomSchedule
import com.rendox.routinetracker.core.database.routine.model.toWeeklySchedule
import com.rendox.routinetracker.core.database.routine.model.toYesNoRoutine
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toInt
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Month

class RoutineLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : RoutineLocalDataSource {

    override suspend fun getRoutineById(routineId: Long): Routine {
        return withContext(dispatcher) {
            db.routineEntityQueries.transactionWithResult {
                val schedule = getScheduleEntity(routineId).toExternalModel { getDueDates(it) }
                getRoutineEntity(routineId).toExternalModel(schedule)
            }
        }
    }

    private fun getRoutineEntity(id: Long): RoutineEntity =
        db.routineEntityQueries.getRoutineById(id).executeAsOne()

    private fun getScheduleEntity(id: Long): ScheduleEntity =
        db.scheduleEntityQueries.getScheduleById(id).executeAsOne()

    private fun getDueDates(scheduleId: Long): List<Int> =
        db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList().map { it.dueDateNumber }

    private fun ScheduleEntity.toExternalModel(dueDatesProvider: (Long) -> List<Int>): Schedule {
        if (type == ScheduleType.EveryDaySchedule) return toEveryDaySchedule()
        val dueDates = dueDatesProvider(id)
        return when (type) {
            ScheduleType.WeeklyScheduleByDueDaysOfWeek -> toWeeklySchedule(dueDates)
            ScheduleType.MonthlyScheduleByDueDaysOfMonth -> {
                val weekDaysMonthRelated = getWeekDayMonthRelatedDays(id)
                toMonthlySchedule(dueDates, weekDaysMonthRelated)
            }

            ScheduleType.PeriodicCustomSchedule -> toPeriodicCustomSchedule(dueDates)
            ScheduleType.CustomDateSchedule -> toCustomDateSchedule(dueDates)
            ScheduleType.AnnualScheduleByDueDaysOfYear -> toAnnualSchedule(dueDates)
            else -> throw IllegalArgumentException()
        }
    }

    private fun RoutineEntity.toExternalModel(schedule: Schedule) = when (this.type) {
        RoutineType.YesNoRoutine -> this.toYesNoRoutine(
            schedule = schedule,
        )
    }

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

    override suspend fun insertRoutine(routine: Routine) {
        return withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                when (routine) {
                    is Routine.YesNoRoutine -> {
                        insertYesNoRoutine(routine)
                        insertSchedule(routine.schedule)
                    }
                }
            }
        }
    }

    @Suppress("UnusedReceiverParameter")
    private fun TransactionWithoutReturn.insertYesNoRoutine(routine: Routine.YesNoRoutine) {
        db.routineEntityQueries.insertRoutine(
            id = routine.id,
            type = RoutineType.YesNoRoutine,
            name = routine.name,
            tasksCompletedCounter = routine.scheduleDeviation,
        )
    }

    private fun insertSchedule(schedule: Schedule) {
        when (schedule) {
            is Schedule.EveryDaySchedule -> insertEveryDaySchedule(schedule)
            is Schedule.WeeklyScheduleByDueDaysOfWeek -> {
                insertWeeklyScheduleByDueDaysOfWeek(schedule)
                insertDueDates(schedule.dueDaysOfWeek.map { it.toInt() })
            }

            is Schedule.WeeklyScheduleByNumOfDueDays -> insertWeeklyScheduleByNumOfDueDays(schedule)

            is Schedule.MonthlyScheduleByDueDatesIndices -> {
                insertMonthlyScheduleByDueDaysOfMonth(schedule)
                val insertedScheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                insertDueDates(schedule.dueDatesIndices)
                insertWeekDaysMonthRelated(insertedScheduleId, schedule.weekDaysMonthRelated)
            }

            is Schedule.MonthlyScheduleByNumOfDueDays -> insertMonthlyScheduleByNumOfDueDays(schedule)

            is Schedule.PeriodicCustomSchedule -> insertPeriodicCustomSchedule(schedule)

            is Schedule.CustomDateSchedule -> {
                insertCustomDateSchedule(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualScheduleByDueDates -> {
                insertAnnualScheduleByDueDaysOfYear(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualScheduleByNumOfDueDays -> insertAnnualScheduleByNumOfDueDays(schedule)
        }
    }

    private fun insertEveryDaySchedule(schedule: Schedule.EveryDaySchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.EveryDaySchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
            startDate = schedule.routineStartDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            scheduleDeviation = 0, // TODO
        )
    }

    private fun insertWeeklyScheduleByDueDaysOfWeek(schedule: Schedule.WeeklyScheduleByDueDaysOfWeek) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklyScheduleByDueDaysOfWeek,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
            startDate = schedule.routineStartDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            scheduleDeviation = 0, // TODO
        )
    }

    private fun insertWeeklyScheduleByNumOfDueDays(schedule: Schedule.WeeklyScheduleByNumOfDueDays) {
        TODO()
    }

    private fun insertMonthlyScheduleByDueDaysOfMonth(schedule: Schedule.MonthlyScheduleByDueDatesIndices) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlyScheduleByDueDaysOfMonth,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = schedule.includeLastDayOfMonth,
            startFromRoutineStartInMonthlySchedule = schedule.startFromRoutineStart,
            startDayOfYearInAnnualSchedule = null,
            startDate = schedule.routineStartDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            scheduleDeviation = 0, // TODO
        )
    }

    private fun insertMonthlyScheduleByNumOfDueDays(schedule: Schedule.MonthlyScheduleByNumOfDueDays) {
        TODO()
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
        TODO()
    }

    private fun insertCustomDateSchedule(schedule: Schedule.CustomDateSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.CustomDateSchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
            startDate = schedule.routineStartDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            scheduleDeviation = 0, // TODO
        )
    }

    private fun insertAnnualScheduleByDueDaysOfYear(schedule: Schedule.AnnualScheduleByDueDates) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualScheduleByDueDaysOfYear,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = AnnualDate(Month.JANUARY, 1), //TODO
            startDate = schedule.routineStartDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
            scheduleDeviation = 0, // TODO
        )
    }

    private fun insertAnnualScheduleByNumOfDueDays(schedule: Schedule.AnnualScheduleByNumOfDueDays) {
        TODO()
    }

    private fun insertDueDates(dueDates: List<Int>) {
        val lastInsertScheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
        for (dueDate in dueDates) {
            db.dueDateEntityQueries.insertDueDate(
                id = null,
                scheduleId = lastInsertScheduleId,
                dueDateNumber = dueDate,
            )
        }
    }

    override suspend fun updateScheduleDeviation(newValue: Int, routineId: Long) {
        TODO()
    }
}