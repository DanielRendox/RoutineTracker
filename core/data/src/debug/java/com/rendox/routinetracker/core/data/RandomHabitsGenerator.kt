package com.rendox.routinetracker.core.data

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.minusDays
import com.rendox.routinetracker.core.logic.time.random
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.logic.time.today
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

class RandomHabitsGenerator(
    private val numOfHabits: Int,
    private val currentDate: LocalDate = today,
    private val startDateRange: LocalDateRange = currentDate.minusDays(365)..currentDate,
) {

    fun generateRandomHabits(): List<Habit> = (1..numOfHabits).map { habitNum ->
        val schedule = generateRandomSchedule()
        Habit.YesNoHabit(
            name = "YesNoHabit $habitNum, ${schedule::class.simpleName}",
            schedule = schedule,
        )
    }

    fun generateCompletionHistory(
        habit: Habit
    ): List<Habit.CompletionRecord> {
        val schedule = habit.schedule
        val entireHistory = (habit.schedule.startDate..currentDate).shuffled()
        val completionRate = when (schedule) {
            is Schedule.EveryDaySchedule -> 0.9F
            is Schedule.WeeklyScheduleByDueDaysOfWeek -> schedule.dueDaysOfWeek.size / 7.0F
            is Schedule.WeeklyScheduleByNumOfDueDays -> schedule.numOfDueDays / 7.0F
            is Schedule.MonthlyScheduleByDueDatesIndices -> schedule.dueDatesIndices.size / 30.0F
            is Schedule.MonthlyScheduleByNumOfDueDays -> schedule.numOfDueDays / 30.0F
            is Schedule.AlternateDaysSchedule -> schedule.numOfDueDays / schedule.numOfDaysInPeriod.toFloat()
            else -> 0.5F
        }
        val completionHistory = entireHistory.take((completionRate * entireHistory.size).roundToInt())
        return completionHistory.map { date ->
            when (habit) {
                is Habit.YesNoHabit -> Habit.YesNoHabit.CompletionRecord(
                    date = date,
                    completed = true,
                )
            }
        }
    }

    private fun generateRandomSchedule(): Schedule {
        val scheduleTypeIndex = Random.nextInt(6)
        val startDate = startDateRange.random()
        if (scheduleTypeIndex == 0) return Schedule.EveryDaySchedule(startDate = startDate)
        if (scheduleTypeIndex == 1) return Schedule.WeeklyScheduleByNumOfDueDays(
            startDate = startDate,
            numOfDueDays = Random.nextInt(1..6),
        )
        if (scheduleTypeIndex == 2) return Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = startDate,
            numOfDueDays = Random.nextInt(1..30),
        )
        val backlogEnabled = Random.nextBoolean()
        val completingAheadEnabled = Random.nextBoolean()
        if (scheduleTypeIndex == 3) return Schedule.WeeklyScheduleByDueDaysOfWeek(
            startDate = startDate,
            dueDaysOfWeek = DayOfWeek.entries.shuffled().take(Random.nextInt(1..6)),
            backlogEnabled = backlogEnabled,
            completingAheadEnabled = completingAheadEnabled,
        )
        if (scheduleTypeIndex == 4) return Schedule.MonthlyScheduleByDueDatesIndices(
            startDate = startDate,
            dueDatesIndices = (1..31).shuffled().take(Random.nextInt(1..30)),
            includeLastDayOfMonth = Random.nextBoolean(),
            backlogEnabled = backlogEnabled,
            completingAheadEnabled = completingAheadEnabled,
        )
        val numOfDueDays = Random.nextInt(1..Schedule.AlternateDaysSchedule.MAX_NUM_OF_DUE_DAYS)
        val numOfNotDueDays =
            Random.nextInt(1..Schedule.AlternateDaysSchedule.MAX_NUM_OF_NOT_DUE_DAYS)
        if (scheduleTypeIndex == 5) return Schedule.AlternateDaysSchedule(
            startDate = startDate,
            numOfDueDays = numOfDueDays,
            numOfDaysInPeriod = numOfDueDays + numOfNotDueDays,
            backlogEnabled = backlogEnabled,
            completingAheadEnabled = completingAheadEnabled,
        )
        throw IllegalStateException("Unknown schedule type index: $scheduleTypeIndex")
    }
}