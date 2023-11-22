package com.rendox.routinetracker.core.domain.completion_time

import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.database.di.toInt
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class GetRoutineCompletionTimeUseCase(
    private val routineRepository: RoutineRepository,
    private val completionTimeRepository: CompletionTimeRepository,
) {
    suspend operator fun invoke(routineId: Long, date: LocalDate): LocalTime? {
        val completionTimeFromSpecificDate =
            completionTimeRepository.getCompletionTime(routineId, date)
        completionTimeFromSpecificDate?.let { return it }

        val routine = routineRepository.getRoutineById(routineId)
        val completionTimeFromSchedule = date.getIndex(routine.schedule)?.let {
            routineRepository.getDueDateSpecificCompletionTime(
                routineId = routineId, dueDateNumber = it
            )
        }
        completionTimeFromSchedule?.let { return it }

        return routine.defaultCompletionTime
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