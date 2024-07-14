package com.rendox.routinetracker.core.database.completionhistory

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.toExternalModel
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.epochDate
import com.rendox.routinetracker.core.model.Habit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : CompletionHistoryLocalDataSource {

    override suspend fun getRecordsInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): List<Habit.CompletionRecord> = withContext(ioDispatcher) {
        db.completionHistoryEntityQueries.getRecordsInPeriod(
            habitId = habit.id!!,
            minDate = period.start,
            maxDate = period.endInclusive,
        ).executeAsList().map { it.toExternalModel(habit) }
    }

    override suspend fun getMultiHabitRecords(
        habitsToPeriods: List<Pair<List<Habit>, LocalDateRange>>,
    ): Map<Long, List<Habit.CompletionRecord>> = withContext(ioDispatcher) {
        require(habitsToPeriods.size <= 5) {
            "Only up to 5 habit types can be queried at once"
        }

        val habits = habitsToPeriods.flatMap { it.first }
        db.completionHistoryEntityQueries
            // this is a nasty workaround because SQLDelight does not support dynamic queries
            .getMultiHabitRecords(
                habitIds1 = habitsToPeriods.getOrNull(0)?.first?.map { it.id!! } ?: emptyList(),
                habitIds2 = habitsToPeriods.getOrNull(1)?.first?.map { it.id!! } ?: emptyList(),
                habitIds3 = habitsToPeriods.getOrNull(2)?.first?.map { it.id!! } ?: emptyList(),
                habitIds4 = habitsToPeriods.getOrNull(3)?.first?.map { it.id!! } ?: emptyList(),
                habitIds5 = habitsToPeriods.getOrNull(4)?.first?.map { it.id!! } ?: emptyList(),
                minDate1 = habitsToPeriods.getOrNull(0)?.second?.start ?: epochDate,
                minDate2 = habitsToPeriods.getOrNull(1)?.second?.start ?: epochDate,
                minDate3 = habitsToPeriods.getOrNull(2)?.second?.start ?: epochDate,
                minDate4 = habitsToPeriods.getOrNull(3)?.second?.start ?: epochDate,
                minDate5 = habitsToPeriods.getOrNull(4)?.second?.start ?: epochDate,
                maxDate1 = habitsToPeriods.getOrNull(0)?.second?.endInclusive ?: epochDate,
                maxDate2 = habitsToPeriods.getOrNull(1)?.second?.endInclusive ?: epochDate,
                maxDate3 = habitsToPeriods.getOrNull(2)?.second?.endInclusive ?: epochDate,
                maxDate4 = habitsToPeriods.getOrNull(3)?.second?.endInclusive ?: epochDate,
                maxDate5 = habitsToPeriods.getOrNull(4)?.second?.endInclusive ?: epochDate,
            )
            .executeAsList()
            .groupBy(
                keySelector = { it.habitId },
                valueTransform = { entity ->
                    when (habits.first { it.id == entity.habitId }) {
                        is Habit.YesNoHabit -> Habit.YesNoHabit.CompletionRecord(
                            date = entity.date,
                            numOfTimesCompleted = entity.numOfTimesCompleted,
                        )
                    }
                },
            )
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

    override suspend fun insertCompletions(completions: Map<Long, List<Habit.CompletionRecord>>) =
        withContext(ioDispatcher) {
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

    override suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    ) = withContext(ioDispatcher) {
        db.completionHistoryEntityQueries.deleteCompletionByDate(habitId, date)
    }
}