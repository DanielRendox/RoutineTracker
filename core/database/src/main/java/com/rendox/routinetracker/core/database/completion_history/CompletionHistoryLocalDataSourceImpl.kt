package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.database.CompletionHistoryEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.habit.model.HabitType
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CompletionHistoryLocalDataSource {
    override suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getNumOfTimesCompletedInPeriod(
                habitId, minDate, maxDate
            ).executeAsOne()
        }
    }

    override suspend fun getRecordByDate(
        habitId: Long, date: LocalDate
    ): Habit.CompletionRecord? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getRecordByDate(
                habitId, date
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastCompletedRecord(habitId: Long): Habit.CompletionRecord? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getLastRecord(habitId)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord> {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getRecordsInPeriod(
                habitId, minDate, maxDate
            ).executeAsList().map { it.toExternalModel()!! }
        }
    }

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        withContext(dispatcher) {
            db.completionHistoryEntityQueries.insertCompletion(
                habitId = habitId,
                date = completionRecord.date,
                numOfTimesCompleted = completionRecord.numOfTimesCompleted,
            )
        }
    }

    override suspend fun deleteCompletionByDate(habitId: Long, date: LocalDate) {
        withContext(dispatcher) {
            db.completionHistoryEntityQueries.deleteCompletionByDate(habitId, date)
        }
    }

    private suspend fun CompletionHistoryEntity.toExternalModel(): Habit.CompletionRecord? {
        val habitType = withContext(dispatcher) {
            db.habitEntityQueries.getHabitById(habitId).executeAsOneOrNull()?.type
        }
        return when (habitType) {
            HabitType.YesNoHabit -> Habit.YesNoHabit.CompletionRecord(
                date = date,
                numOfTimesCompleted = numOfTimesCompleted,
            )

            null -> null
        }
    }
}