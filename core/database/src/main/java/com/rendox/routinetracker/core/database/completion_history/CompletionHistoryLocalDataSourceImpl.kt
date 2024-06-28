package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.toExternalModel
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : CompletionHistoryLocalDataSource {

    override suspend fun getRecordsInPeriod(
        habit: Habit,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord> {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getRecordsInPeriod(
                habit.id!!, minDate, maxDate
            ).executeAsList().map { it.toExternalModel(habit) }
        }
    }

    override suspend fun getAllRecords(): Map<Long, List<Habit.CompletionRecord>> {
        return withContext(ioDispatcher) {
            db.completionHistoryEntityQueries.getAllRecords().executeAsList().groupBy(
                keySelector = { it.habitId },
                valueTransform = {
                    Habit.YesNoHabit.CompletionRecord(
                        date = it.date,
                        numOfTimesCompleted = it.numOfTimesCompleted,
                    )
                },
            )
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
}