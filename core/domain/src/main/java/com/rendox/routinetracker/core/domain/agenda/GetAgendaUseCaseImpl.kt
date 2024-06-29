package com.rendox.routinetracker.core.domain.agenda

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.habit_status.HabitStatusComputer
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitCompletionData
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    ): Map<Habit, HabitCompletionData> = withContext(Dispatchers.IO) {
        val habits = habitRepository.getAllOngoingHabits(validationDate)
        if (habits.isEmpty()) return@withContext emptyMap()

        val periodsToQuery = habits.groupByPeriods(validationDate)
        val completionHistory = completionHistoryRepository.getMultiHabitRecords(periodsToQuery)
        val vacationHistory = vacationRepository.getMultiHabitVacations(
            habitsToPeriods = periodsToQuery.map { (habits, period) ->
                habits.map { it.id!! } to period
            }
        )

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

    companion object {
        /*
        * There may be too many entries so retrieving all of them at once may be inefficient.
        * We must retrieve only ones that are in the validation date's period.
        *
        * At the same time, getting completions for each habit separately may be
        * inefficient as well, especially when there are lots of them.
        *
        * So we do something in between. We group habits by their schedule and get completions
        * for each group separately. This works because habits with the same schedule type will
        * have about the same period.
        *
        * This function computes this period for each group.
        */
        private fun List<Habit>.groupByPeriods(
            validationDate: LocalDate
        ): List<Pair<List<Habit>, LocalDateRange>> = groupBy { habit ->
            val schedule = habit.schedule
            when (schedule) {
                is Schedule.NonPeriodicSchedule -> 0
                is Schedule.WeeklySchedule -> 1
                is Schedule.MonthlySchedule -> 2
                is Schedule.AnnualSchedule -> 3
                is Schedule.AlternateDaysSchedule -> 4
            }
        }.map { (_, habits) ->
            val periods = habits.map { habit ->
                when (val schedule = habit.schedule) {
                    is Schedule.PeriodicSchedule -> schedule.getPeriodRange(validationDate)!!
                    is Schedule.NonPeriodicSchedule -> validationDate..validationDate
                }
            }
            val minDate = periods.minOf { it.start }
            val maxDate = periods.maxOf { it.endInclusive }
            habits to minDate..maxDate
        }
    }
}