package com.rendox.routinetracker.core.domain.agenda

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.habit_status.HabitStatusComputer
import com.rendox.routinetracker.core.logic.measureTimeMillisForResult
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitCompletionData
import kotlinx.datetime.LocalDate

class GetAgendaUseCaseImpl(
    private val habitRepository: HabitRepository,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val habitStatusComputer: HabitStatusComputer,
) : GetAgendaUseCase {

    override suspend fun invoke(
        validationDate: LocalDate,
        today: LocalDate,
    ): Map<Habit, HabitCompletionData> {
        val (habits, habitsRetrievalDuration) = measureTimeMillisForResult {
            habitRepository.getAllOngoingHabits(validationDate)
        }
        println("GetAgendaUseCase retrieved habits in $habitsRetrievalDuration ms")
        val (completionHistory, completionsRetrievalDuration) = measureTimeMillisForResult {
            completionHistoryRepository.getAllRecords()
        }
        println("GetAgendaUseCase retrieved completions in $completionsRetrievalDuration ms")
        val (vacationHistory, vacationsRetrievalDuration) = measureTimeMillisForResult {
            vacationRepository.getAllVacations()
        }
        println("GetAgendaUseCase retrieved vacation history in $vacationsRetrievalDuration ms")
        val (result, statusComputationDuration) = measureTimeMillisForResult {
            habits.associateWith { habit ->
                val habitCompletionHistory = completionHistory[habit.id!!] ?: emptyList()
                val status = habitStatusComputer.computeStatus(
                    validationDate = validationDate,
                    today = today,
                    habit = habit,
                    completionHistory = habitCompletionHistory,
                    vacationHistory = vacationHistory[habit.id!!] ?: emptyList(),
                )
                val numOfTimesCompleted = habitCompletionHistory.find {
                    it.date == validationDate
                }?.numOfTimesCompleted ?: 0f
                HabitCompletionData(status, numOfTimesCompleted)
            }
        }
        println("GetAgendaUseCase computed statuses in $statusComputationDuration ms")
        return result
    }
}