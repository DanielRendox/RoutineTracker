package com.rendox.routinetracker.core.data.streaks

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

/**
 * The app computes streaks for each schedule period and cashes them to optimize performance so
 * that there is no need to compute the streaks again later. This class serves as an access point
 * to the cashed streaks.
 *
 * Note that there may be no cashed streaks for the period not because they were not computed
 * yet but because the user's completions did not form any streaks in that period.
 * In this case, this period is still stored as a cashed period.
 *
 */
interface StreakRepository {

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
    suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>,
    )

    suspend fun getAllStreaks(habitId: Long): List<Streak>

    /**
     * Returns all streaks that either are within [minDate] and [maxDate] or have at least
     * some dates that are within that period.
     */
    suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Streak>

    suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange>

    /**
     * @param dateInPeriod any date that is within the period
     */
    suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange?

    suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate,
    )
}