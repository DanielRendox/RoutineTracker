package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputerImpl
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class GetAllStreaksUseCaseImpl(
    private val getHabit: GetHabitUseCase,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val vacationHistoryRepository: VacationRepository,
    private val defaultDispatcher: CoroutineContext,
) : GetAllStreaksUseCase {

    override suspend operator fun invoke(
        habitId: Long,
        today: LocalDate,
    ): List<Streak> = withContext(defaultDispatcher) {
        val completionHistory = completionHistoryRepository.getRecordsInPeriod(habitId = habitId)
        val vacationHistory = vacationHistoryRepository.getVacationsInPeriod(habitId = habitId)
        val habit = getHabit(habitId)

        val habitStatusComputer: HabitStatusComputer = HabitStatusComputerImpl(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
        val streakComputer: StreakComputer = StreakComputerImpl(
            habit = habit,
            completionHistory = completionHistory,
            habitStatusComputer = habitStatusComputer,
        )
        streakComputer.computeAllStreaks(today)
    }
}