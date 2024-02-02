package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.isSubsetOf
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class StreakRepositoryFake(
    private val habitData: HabitData
) : StreakRepository {

    /**
     * Inserts a comprehensive list of streaks for the given [periods]. The comprehensive list
     * means that all streaks for the given period were computed and now they are cashed so that
     * next time they shouldn't be computed again. If you insert only some streaks for the period,
     * and not a comprehensive list, don't include the corresponding period in the [periods] list.
     * Otherwise, this may break the logic of the program.
     *
     * It's totally okay to pass an empty list of [streaks] only to preserve the fact that the user's
     * completions didn't form any streaks in the given [periods].
     */
    override suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>
    ) {
        habitData.streaks.update {
            it.toMutableList().apply { addAll(streaks) }
        }
        habitData.streakCashedPeriods.update {
            it.toMutableList().apply { addAll(periods) }.distinct()
        }
    }

    override suspend fun getAllStreaks(habitId: Long): List<Streak> =
        habitData.streaks.value.filter { it.first == habitId }.map { it.second }

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate
    ): List<Streak> = habitData.streaks.value.filter {
        it.first == habitId &&
                it.second.startDate <= maxDate && it.second.endDate >= minDate
    }.map { it.second }

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> =
        habitData.streakCashedPeriods.value.map { it.second }

    override suspend fun getCashedPeriod(
        habitId: Long, dateInPeriod: LocalDate
    ): LocalDateRange? = habitData.streakCashedPeriods.value.find {
        it.first == habitId && it.second.contains(dateInPeriod)
    }?.second

    override suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate
    ) = habitData.streakCashedPeriods.update {  streaks ->
        streaks.toMutableList().apply {
            removeAll {
                it.first == habitId && it.second.isSubsetOf(periodStartDate..periodEndDate)
            }
        }
    }
}