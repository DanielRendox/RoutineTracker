package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.TransactionWithoutReturn
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.ScheduleType
import com.rendox.routinetracker.core.database.model.toEveryDaySchedule
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
import com.rendox.routinetracker.core.database.model.toCustomDateSchedule
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class RoutineLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : RoutineLocalDataSource {

    override fun getRoutineById(id: Long): Flow<Routine?> {
        return db.routineEntityQueries.transactionWithResult {
            getScheduleEntity(id).combine(getRoutineEntity(id)) { scheduleEntity, routineEntity ->
                val schedule = scheduleEntity.toExternalModel { getDueDates(it) }
                routineEntity.toExternalModel(schedule)
            }
        }
    }

    private fun getRoutineEntity(id: Long): Flow<RoutineEntity> =
        db.routineEntityQueries.getRoutineById(id).asFlow().mapToOne(dispatcher)

    private fun getScheduleEntity(id: Long): Flow<ScheduleEntity> =
        db.scheduleEntityQueries.getScheduleById(id).asFlow().mapToOne(dispatcher)

    private fun getDueDates(scheduleId: Long): List<Int> =
        db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList().map { it.dueDateNumber }

    private fun ScheduleEntity.toExternalModel(dueDatesProvider: (Long) -> List<Int>): Schedule {
        if (type == ScheduleType.EveryDaySchedule) return toEveryDaySchedule()
        val dueDates = dueDatesProvider(id)
        return when (type) {
            ScheduleType.WeeklySchedule -> toWeeklySchedule(dueDates)
            ScheduleType.MonthlySchedule -> {
                val weekDaysMonthRelated = getWeekDayMonthRelatedDays(id)
                toMonthlySchedule(dueDates, weekDaysMonthRelated)
            }

            ScheduleType.PeriodicCustomSchedule -> toPeriodicCustomSchedule(dueDates)
            ScheduleType.CustomDateSchedule -> toCustomDateSchedule(dueDates)
            ScheduleType.AnnualSchedule -> toAnnualSchedule(dueDates)
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
        )
    }

    private fun insertSchedule(schedule: Schedule) {
        when (schedule) {
            is Schedule.EveryDaySchedule -> insertEveryDaySchedule(schedule)
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
                insertCustomDateSchedule(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }

            is Schedule.AnnualSchedule -> {
                insertAnnualSchedule(schedule)
                insertDueDates(schedule.dueDates.map { it.toInt() })
            }
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
        )
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
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
            startDate = schedule.startDate,
            scheduleDeviation = schedule.scheduleDeviation,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.cancelDuenessIfDoneAhead,
            vacationStartDate = schedule.vacationStartDate,
            vacationEndDate = schedule.vacationEndDate,
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