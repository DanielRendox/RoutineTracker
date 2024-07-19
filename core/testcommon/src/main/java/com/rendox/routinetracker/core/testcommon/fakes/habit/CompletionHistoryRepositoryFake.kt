package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.logic.contains
import com.rendox.routinetracker.core.logic.isSubsetOf
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryFake(
    private val habitData: HabitData,
) : CompletionHistoryRepository {

    override suspend fun getRecordsInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): List<Habit.CompletionRecord> = habitData.completionHistory.value.filter {
        it.first == habit.id!! && it.second.date in period
    }.map { it.second }.sortedBy { it.date }

    override suspend fun getMultiHabitRecords(
        habitsToPeriods: List<Pair<List<Habit>, LocalDateRange>>,
    ): Map<Long, List<Habit.CompletionRecord>> = getAllRecords()

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) = habitData.completionHistory.update {
        it.toMutableList().apply { add(habitId to completionRecord) }
    }

    override suspend fun insertCompletions(completions: Map<Long, List<Habit.CompletionRecord>>) {
        habitData.completionHistory.update {
            it.toMutableList().apply {
                for ((habitId, completionRecords) in completions) {
                    for (completionRecord in completionRecords) {
                        add(habitId to completionRecord)
                    }
                }
            }
        }
    }

    override suspend fun getRecordsWithoutStreaks(habit: Habit): List<Habit.CompletionRecord> =
        habitData.completionHistory.value.filter { (id, completion) ->
            id == habit.id!! &&
                !habitData.streaks.value.any { streak ->
                    streak.first == habit.id!! && streak.second.contains(completion.date)
                }
        }.map { it.second }

    override suspend fun insertCompletionAndCacheStreaks(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) {
        insertCompletion(habitId, completionRecord)
        habitData.streaks.update { cachedStreaks ->
            cachedStreaks.toMutableList().apply {
                removeAll {
                    it.first == habitId && it.second.isSubsetOf(period)
                }
                addAll(streaks.map { habitId to it })
            }
        }
    }

    override suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    ) {
        habitData.completionHistory.update { completionHistory ->
            val completionIndex = completionHistory.indexOfFirst {
                it.first == habitId && it.second.date == date
            }
            if (completionIndex != -1) {
                completionHistory.toMutableList().apply { removeAt(completionIndex) }
            } else {
                completionHistory
            }
        }
    }

    private fun getAllRecords(): Map<Long, List<Habit.CompletionRecord>> = habitData.completionHistory.value.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second },
    )
}