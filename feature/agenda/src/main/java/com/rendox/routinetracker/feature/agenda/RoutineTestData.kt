package com.rendox.routinetracker.feature.agenda

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

val habitLists = listOf<Habit>(
    Habit.YesNoHabit(
        name = "Do sports",
        description = "Stay fit and healthy",
        sessionDurationMinutes = 75,
        defaultCompletionTime = LocalTime(17, 0),
        schedule = Schedule.EveryDaySchedule(
            startDate = LocalDate(2023, 11, 10),
        )
    ),
    Habit.YesNoHabit(
        name = "Work on my app",
        description = null,
        sessionDurationMinutes = 300,
        defaultCompletionTime = LocalTime(10, 0),
        schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            startDate = LocalDate(2023, 11, 10),
            dueDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        )
    )
)