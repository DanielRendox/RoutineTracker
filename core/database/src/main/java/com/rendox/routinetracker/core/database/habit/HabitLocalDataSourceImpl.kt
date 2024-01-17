package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.habit.model.HabitType
import com.rendox.routinetracker.core.database.habit.model.toExternalModel
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class HabitLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
    private val scheduleLocalDataSource: ScheduleLocalDataSource,
) : HabitLocalDataSource {

    override suspend fun insertHabit(habit: Habit) = withContext(ioDispatcher) {
        db.habitEntityQueries.transaction {
            when (habit) {
                is Habit.YesNoHabit -> {
                    insertYesNoHabit(habit)
                    scheduleLocalDataSource.insertSchedule(habit.schedule)
                }
            }
        }
    }

    private fun insertYesNoHabit(habit: Habit.YesNoHabit) {
        db.habitEntityQueries.insertHabit(
            id = habit.id,
            type = HabitType.YesNoHabit,
            name = habit.name,
            description = habit.description,
            sessionDurationMinutes = habit.sessionDurationMinutes,
            progress = habit.progress,
            defaultCompletionTimeHour = habit.defaultCompletionTime?.hour,
            defaultCompletionTimeMinute = habit.defaultCompletionTime?.minute,
        )
    }

    override suspend fun getHabitById(habitId: Long): Habit = withContext(ioDispatcher) {
        db.habitEntityQueries.transactionWithResult {
            val schedule = scheduleLocalDataSource.getScheduleById(habitId)
            db.habitEntityQueries.getHabitById(habitId).executeAsOne().toExternalModel(schedule)
        }
    }

    override suspend fun getAllHabits(): List<Habit> = withContext(ioDispatcher) {
        db.habitEntityQueries.transactionWithResult {
            db.habitEntityQueries.getAllHabits()
                .executeAsList()
                .map { habitEntity ->
                    val schedule = scheduleLocalDataSource.getScheduleById(habitEntity.id)
                    habitEntity.toExternalModel(schedule)
                }
        }
    }

    override suspend fun deleteHabitById(habitId: Long) {
        db.habitEntityQueries.transaction {
            db.habitEntityQueries.deleteHabitById(habitId)
            scheduleLocalDataSource.deleteSchedule(habitId)
        }
    }
}