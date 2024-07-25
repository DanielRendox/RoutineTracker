package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.schedule.expandPeriodToScheduleBounds
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class GetStreaksInPeriodUseCase(
    completionHistoryRepository: CompletionHistoryRepository,
    vacationRepository: VacationRepository,
    streakComputer: StreakComputer,
) : StreakManager(
    completionHistoryRepository,
    vacationRepository,
    streakComputer,
) {
    suspend operator fun invoke(
        habit: Habit,
        period: LocalDateRange,
        today: LocalDate,
    ): List<Streak> {
        val expandedPeriod = period.expandPeriodToScheduleBounds(
            schedule = habit.schedule,
            getPeriodRange = { date -> getPeriodRange(habit.schedule, date) },
        )
        return computeStreaks(habit, expandedPeriod, today)
    }
}