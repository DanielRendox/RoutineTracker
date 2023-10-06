package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.TransactionWithReturn
import app.cash.sqldelight.TransactionWithoutReturn
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.ScheduleType
import com.rendox.routinetracker.core.database.model.constructCustomDateSchedule
import com.rendox.routinetracker.core.database.model.constructEveryDaySchedule
import com.rendox.routinetracker.core.database.model.toMonthlySchedule
import com.rendox.routinetracker.core.database.model.toPeriodicCustomSchedule
import com.rendox.routinetracker.core.database.model.toWeeklySchedule
import com.rendox.routinetracker.core.database.model.toYesNoRoutine
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toInt
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.database.model.RoutineType
import com.rendox.routinetracker.core.database.model.toAnnualSchedule
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RoutineLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : RoutineLocalDataSource {

    override suspend fun getRoutineById(id: Long): Routine? {
        return withContext(dispatcher) {
            db.routineEntityQueries.transactionWithResult {
                val schedule: Schedule = getScheduleEntity(id).toExternalModel { getDueDates(it) }
                getRoutineEntity(id).toExternalModel(schedule)
            }
        }
    }

    @Suppress("UnusedReceiverParameter")
    private fun TransactionWithReturn<Routine?>.getRoutineEntity(id: Long): RoutineEntity =
        db.routineEntityQueries.getRoutineById(id).executeAsOne()

    @Suppress("UnusedReceiverParameter")
    private fun TransactionWithReturn<Routine?>.getScheduleEntity(id: Long): ScheduleEntity =
        db.scheduleEntityQueries.getScheduleById(id).executeAsOne()

    @Suppress("UnusedReceiverParameter")
    private fun TransactionWithReturn<Routine?>.getDueDates(scheduleId: Long): List<Int> =
        db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList().map { it.dueDateNumber }

    private fun ScheduleEntity.toExternalModel(dueDatesProvider: (Long) -> List<Int>): Schedule {
        if (type == ScheduleType.EveryDaySchedule) return constructEveryDaySchedule()
        val dueDates = dueDatesProvider(id)
        return when (type) {
            ScheduleType.WeeklySchedule -> this.toWeeklySchedule(dueDates)
            ScheduleType.MonthlySchedule -> {
                val weekDaysMonthRelated = getWeekDayMonthRelatedDays(id)
                this.toMonthlySchedule(dueDates, weekDaysMonthRelated)
            }

            ScheduleType.PeriodicCustomSchedule -> this.toPeriodicCustomSchedule(dueDates)
            ScheduleType.CustomDateSchedule -> constructCustomDateSchedule(dueDates)
            ScheduleType.AnnualSchedule -> this.toAnnualSchedule(dueDates)
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
            startDate = routine.startDate,
            backlogEnabled = routine.backlogEnabled,
            periodSeparation = null,
            vacationStartDate = routine.vacationStartDate,
            vacationEndDate = routine.vacationEndDate,
        )
    }

    private fun insertSchedule(schedule: Schedule) {
        when (schedule) {
            is Schedule.EveryDaySchedule -> insertEveryDaySchedule()
            is Schedule.WeeklySchedule -> {
                insertWeeklySchedule(schedule)
                insertDueDates(schedule.dueDaysOfWeek.map { it.toInt() })
            }

            is Schedule.MonthlySchedule -> {
                insertMonthlySchedule(schedule)
                val insertedScheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                insertDueDates(schedule.dueDatesIndices)
                insertWeekDaysMonthRelated(insertedScheduleId, schedule.weekDaysMonthRelated)
            }

            is Schedule.PeriodicCustomSchedule -> {
                insertPeriodicCustomSchedule(schedule)
                insertDueDates(schedule.dueDatesIndices)
            }

            is Schedule.CustomDateSchedule -> {
                insertCustomDateSchedule()
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualSchedule -> {
                insertAnnualSchedule(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }
        }
    }

    private fun insertEveryDaySchedule() {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.EveryDaySchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
        )
    }

    private fun insertWeeklySchedule(schedule: Schedule.WeeklySchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklySchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
        )
    }

    private fun insertMonthlySchedule(schedule: Schedule.MonthlySchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlySchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = schedule.includeLastDayOfMonth,
            startFromRoutineStartInMonthlySchedule = schedule.startFromRoutineStart,
            startDayOfYearInAnnualSchedule = null,
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
            numOfDaysInPeriodicSchedule = schedule.numOfDaysInPeriod,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
        )
    }

    private fun insertCustomDateSchedule() {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.CustomDateSchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = null,
        )
    }

    private fun insertAnnualSchedule(schedule: Schedule.AnnualSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualSchedule,
            numOfDaysInPeriodicSchedule = null,
            startDayOfWeekInWeeklySchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            startFromRoutineStartInMonthlySchedule = null,
            startDayOfYearInAnnualSchedule = schedule.startDayOfYear,
        )
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
}