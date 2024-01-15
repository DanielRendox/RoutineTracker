package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.model.HabitCompletionData
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class GetHabitCompletionDataUseCaseImpl(
    private val getHabit: GetHabitUseCase,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val habitStatusComputer: HabitStatusComputer,
    private val defaultDispatcher: CoroutineContext,
) : GetHabitCompletionDataUseCase {
    override suspend operator fun invoke(
        habitId: Long,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitCompletionData = invoke(
        habitId = habitId,
        validationDates = listOf(validationDate),
        today = today,
    ).values.first()

    override suspend operator fun invoke(
        habitId: Long,
        validationDates: Iterable<LocalDate>,
        today: LocalDate,
    ): Map<LocalDate, HabitCompletionData> = withContext(defaultDispatcher) {
        val completionHistory = completionHistoryRepository.getRecordsInPeriod(habitId = habitId)
        validationDates.associateWith { date ->
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = date,
                today = today,
                habit = getHabit(habitId),
                completionHistory = completionHistory,
                vacationHistory = vacationRepository.getVacationsInPeriod(habitId = habitId),
            )
            val numOfTimesCompleted =
                completionHistory.find { it.date == date }?.numOfTimesCompleted ?: 0f
            HabitCompletionData(
                habitStatus = habitStatus,
                numOfTimesCompleted = numOfTimesCompleted,
            )
        }
    }
}