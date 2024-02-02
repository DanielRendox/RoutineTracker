package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.toInt
import com.rendox.routinetracker.core.database.habit.model.ScheduleType
import com.rendox.routinetracker.core.database.habit.model.toExternalModel
import com.rendox.routinetracker.core.model.Schedule

internal class ScheduleLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dueDateLocalDataSource: DueDateLocalDataSource,
    private val weekDaysMonthRelatedLocalDataSource: WeekDaysMonthRelatedLocalDataSource,
): ScheduleLocalDataSource {
    override fun insertSchedule(schedule: Schedule) {
        when (schedule) {
            is Schedule.EveryDaySchedule ->
                insertEveryDaySchedule(schedule)

            is Schedule.WeeklyScheduleByDueDaysOfWeek -> {
                insertWeeklyScheduleByDueDaysOfWeek(schedule)
                val scheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                dueDateLocalDataSource.insertDueDates(
                    dueDates = schedule.dueDaysOfWeek.map { it.toInt() },
                    scheduleId = scheduleId,
                )
            }

            is Schedule.WeeklyScheduleByNumOfDueDays ->
                insertWeeklyScheduleByNumOfDueDays(schedule)

            is Schedule.MonthlyScheduleByDueDatesIndices -> {
                insertMonthlyScheduleByDueDatesIndices(schedule)
                val scheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                dueDateLocalDataSource.insertDueDates(
                    scheduleId = scheduleId,
                    dueDates = schedule.dueDatesIndices,
                )
                weekDaysMonthRelatedLocalDataSource.insertWeekDaysMonthRelated(
                    scheduleId = scheduleId,
                    weekDaysMonthRelated = schedule.weekDaysMonthRelated,
                )
            }

            is Schedule.MonthlyScheduleByNumOfDueDays ->
                insertMonthlyScheduleByNumOfDueDays(schedule)

            is Schedule.AlternateDaysSchedule ->
                insertAlternateDaysSchedule(schedule)

            is Schedule.CustomDateSchedule -> {
                insertCustomDateSchedule(schedule)
                val scheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                dueDateLocalDataSource.insertDueDates(
                    dueDates = schedule.dueDates.map { it.toInt() },
                    scheduleId = scheduleId,
                )
            }

            is Schedule.AnnualScheduleByDueDates -> {
                insertAnnualScheduleByDueDates(schedule)
                val scheduleId = db.scheduleEntityQueries.lastInsertRowId().executeAsOne()
                dueDateLocalDataSource.insertDueDates(
                    dueDates = schedule.dueDates.map { it.toInt() },
                    scheduleId = scheduleId,
                )
            }

            is Schedule.AnnualScheduleByNumOfDueDays ->
                insertAnnualScheduleByNumOfDueDays(schedule)
        }
    }

    private fun insertEveryDaySchedule(schedule: Schedule.EveryDaySchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.EveryDaySchedule,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = null,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertWeeklyScheduleByDueDaysOfWeek(schedule: Schedule.WeeklyScheduleByDueDaysOfWeek) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklyScheduleByDueDaysOfWeek,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            startFromHabitStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertWeeklyScheduleByNumOfDueDays(schedule: Schedule.WeeklyScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.WeeklyScheduleByNumOfDueDays,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = schedule.startDayOfWeek,
            startFromHabitStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertMonthlyScheduleByDueDatesIndices(schedule: Schedule.MonthlyScheduleByDueDatesIndices) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlyScheduleByDueDatesIndices,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = schedule.startFromHabitStart,
            includeLastDayOfMonthInMonthlySchedule = schedule.includeLastDayOfMonth,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = null
        )
    }

    private fun insertMonthlyScheduleByNumOfDueDays(schedule: Schedule.MonthlyScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.MonthlyScheduleByNumOfDueDays,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = schedule.startFromHabitStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertAlternateDaysSchedule(schedule: Schedule.AlternateDaysSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AlternateDaysSchedule,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = schedule.numOfDaysInPeriod,
        )
    }

    private fun insertCustomDateSchedule(schedule: Schedule.CustomDateSchedule) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.CustomDateSchedule,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = null,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = null,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertAnnualScheduleByDueDates(schedule: Schedule.AnnualScheduleByDueDates) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualScheduleByDueDates,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = schedule.startFromHabitStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = null,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    private fun insertAnnualScheduleByNumOfDueDays(schedule: Schedule.AnnualScheduleByNumOfDueDays) {
        db.scheduleEntityQueries.insertSchedule(
            id = null,
            type = ScheduleType.AnnualScheduleByNumOfDueDays,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            backlogEnabled = schedule.backlogEnabled,
            cancelDuenessIfDoneAhead = schedule.completingAheadEnabled,
            startDayOfWeekInWeeklySchedule = null,
            startFromHabitStartInMonthlyAndAnnualSchedule = schedule.startFromHabitStart,
            includeLastDayOfMonthInMonthlySchedule = null,
            periodicSeparationEnabledInPeriodicSchedule = schedule.periodSeparationEnabled,
            numOfDueDaysInByNumOfDueDaysSchedule = schedule.numOfDueDays,
            numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = schedule.numOfDueDaysInFirstPeriod,
            numOfDaysInAlternateDaysSchedule = null,
        )
    }

    override fun getScheduleById(scheduleId: Long): Schedule =
        db.scheduleEntityQueries.getScheduleById(scheduleId).executeAsOne().toExternalModel(
            dueDatesProvider = { dueDateLocalDataSource.getDueDates(it) },
            weekDaysMonthRelatedProvider = {
                weekDaysMonthRelatedLocalDataSource.getWeekDayMonthRelatedDays(it)
            }
        )

    override fun getAllSchedules(): List<Schedule> =
        db.scheduleEntityQueries.getAllSchedules().executeAsList().map { scheduleEntity ->
            scheduleEntity.toExternalModel(
                dueDatesProvider = { dueDateLocalDataSource.getDueDates(it) },
                weekDaysMonthRelatedProvider = {
                    weekDaysMonthRelatedLocalDataSource.getWeekDayMonthRelatedDays(it)
                }
            )
        }

    override fun deleteSchedule(scheduleId: Long) {
        db.scheduleEntityQueries.deleteSchedule(scheduleId)
        dueDateLocalDataSource.deleteDueDates(scheduleId)
        weekDaysMonthRelatedLocalDataSource.deleteWeekDayMonthRelatedDays(scheduleId)
    }
}