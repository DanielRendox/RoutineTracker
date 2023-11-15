package com.rendox.routinetracker.feature.agenda

import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val routineList = listOf<Routine>(
    Routine.YesNoRoutine(
        name = "Do sports",
        description = "Stay fit and healthy",
        sessionDurationMinutes = 75,
        defaultCompletionTime = LocalTime(17, 0),
        schedule = Schedule.EveryDaySchedule(
            routineStartDate = LocalDate(2023, 11, 10),
        )
    ),
    Routine.YesNoRoutine(
        name = "Work on my app",
        description = null,
        sessionDurationMinutes = 300,
        defaultCompletionTime = LocalTime(10, 0),
        schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            routineStartDate = LocalDate(2023, 11, 10),
            dueDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        )
    )
)