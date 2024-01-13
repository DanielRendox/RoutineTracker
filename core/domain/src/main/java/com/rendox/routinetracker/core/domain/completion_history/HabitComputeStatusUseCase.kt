package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.model.HabitStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

/**
 * This is a helper class for testing the [HabitStatusComputerImpl] class.
 */
class HabitComputeStatusUseCase(
    private val habitRepository: HabitRepository,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val defaultDispatcher: CoroutineContext = Dispatchers.Default,
) {
    suspend operator fun invoke(
        habitId: Long,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitStatus {
        val habitStatusComputer = HabitStatusComputerImpl(
            habit = habitRepository.getHabitById(habitId),
            completionHistory = completionHistoryRepository.getRecordsInPeriod(habitId = habitId),
            vacationHistory = vacationRepository.getVacationsInPeriod(habitId = habitId),
            defaultDispatcher = defaultDispatcher,
        )
        return habitStatusComputer.computeStatus(validationDate, today)
    }

    suspend operator fun invoke(
        habitId: Long,
        validationDates: Iterable<LocalDate>,
        today: LocalDate,
    ): Map<LocalDate, HabitStatus> {
        val habitStatusComputer = HabitStatusComputerImpl(
            habit = habitRepository.getHabitById(habitId),
            completionHistory = completionHistoryRepository.getRecordsInPeriod(habitId = habitId),
            vacationHistory = vacationRepository.getVacationsInPeriod(habitId = habitId),
            defaultDispatcher = defaultDispatcher,
        )
        return validationDates.associateWith {
            habitStatusComputer.computeStatus(
                validationDate = it,
                today = today,
            )
        }
    }
}