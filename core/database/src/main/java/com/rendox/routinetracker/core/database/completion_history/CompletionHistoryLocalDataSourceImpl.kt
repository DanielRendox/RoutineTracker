package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.database.CompletionHistoryEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.habit.model.HabitType
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : CompletionHistoryLocalDataSource {
    override suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getNumOfTimesCompletedInPeriod(
                habitId, minDate, maxDate
            ).executeAsOne()
        }
    }

    override suspend fun getRecordByDate(
        habitId: Long, date: LocalDate
    ): Habit.CompletionRecord? {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getRecordByDate(
                habitId, date
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ): Habit.CompletionRecord? {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getLastRecord(habitId, minDate, maxDate)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getFirstCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Habit.CompletionRecord? {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getFirstRecord(habitId, minDate, maxDate)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord> {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getRecordsInPeriod(
                habitId, minDate, maxDate
            ).executeAsList().map { it.toExternalModel()!! }
        }
    }

    override suspend fun getAllRecords(): List<Pair<Long, Habit.CompletionRecord>> {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getAllRecords().executeAsList().map {
                it.habitId to it.toExternalModel()!!
            }
        }
    }

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.insertCompletion(
                habitId = habitId,
                date = completionRecord.date,
                numOfTimesCompleted = completionRecord.numOfTimesCompleted,
            )
        }
    }

    override suspend fun insertCompletions(
        completions: Map<Long, List<Habit.CompletionRecord>>
    ) = withContext(ioDispatcher) {
        println("CompletionHistoryLocalDataSourceImpl.insertCompletions()")
        db.completionHistoryEntityQueries.transaction {
            for ((habitId, completionRecords) in completions) {
                for (completion in completionRecords) {
                    db.completionHistoryEntityQueries.insertCompletion(
                        habitId = habitId,
                        date = completion.date,
                        numOfTimesCompleted = completion.numOfTimesCompleted,
                    )
                }
            }
        }
    }

    override suspend fun deleteCompletionByDate(habitId: Long, date: LocalDate) {
        withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.deleteCompletionByDate(habitId, date)
        }
    }

    private suspend fun CompletionHistoryEntity.toExternalModel(): Habit.CompletionRecord? {
        val habitType = withContext(ioDispatcher) {
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