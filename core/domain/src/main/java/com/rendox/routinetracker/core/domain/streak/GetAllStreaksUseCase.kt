package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputerImpl
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class GetAllStreaksUseCase(
    private val habitRepository: HabitRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val habitComputeStatusUseCase: HabitComputeStatusUseCase,
    private val vacationHistoryRepository: VacationRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    suspend operator fun invoke(
        habitId: Long,
        today: LocalDate,
    ): List<Streak> = withContext(defaultDispatcher) {
        val completionHistory = completionHistoryRepository.getRecordsInPeriod(habitId = habitId)
        if (completionHistory.isEmpty()) return@withContext emptyList<Streak>()
        val vacationHistory = vacationHistoryRepository.getVacationsInPeriod(habitId = habitId)

        val habit = habitRepository.getHabitById(habitId)
        val habitStatusComputer = HabitStatusComputerImpl(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            defaultDispatcher = defaultDispatcher,
        )
        val streakComputer = StreakComputerImpl(
            habit = habit,
            completionHistory = completionHistory,
            habitStatusComputer = habitStatusComputer,
        )
        streakComputer.computeAllStreaks(today)
    }
}