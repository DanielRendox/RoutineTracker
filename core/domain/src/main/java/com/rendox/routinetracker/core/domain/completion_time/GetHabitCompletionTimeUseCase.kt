package com.rendox.routinetracker.core.domain.completion_time

import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.database.di.toInt
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class GetHabitCompletionTimeUseCase(
    private val habitRepository: HabitRepository,
    private val completionTimeRepository: CompletionTimeRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate): LocalTime? {
        val completionTimeFromSpecificDate =
            completionTimeRepository.getCompletionTime(habitId, date)
        completionTimeFromSpecificDate?.let { return it }

        val habit = habitRepository.getHabitById(habitId)
        val completionTimeFromSchedule = date.getIndex(habit.schedule)?.let {
            habitRepository.getDueDateSpecificCompletionTime(
                routineId = habitId, dueDateNumber = it
            )
        }
        completionTimeFromSchedule?.let { return it }

        return habit.defaultCompletionTime
    }

    private fun LocalDate.getIndex(schedule: Schedule): Int? {
        return when (schedule) {
            is Schedule.EveryDaySchedule -> null
            is Schedule.ByNumOfDueDays -> null
            is Schedule.WeeklyScheduleByDueDaysOfWeek -> dayOfWeek.toInt()
            is Schedule.MonthlyScheduleByDueDatesIndices -> dayOfMonth
            is Schedule.AnnualScheduleByDueDates -> AnnualDate(month, dayOfMonth).toInt()
            is Schedule.CustomDateSchedule -> this.toInt()
        }
    }
}