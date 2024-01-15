package com.rendox.routinetracker.core.domain.streak

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputerImpl
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StreakComputerImplTest {

    private val habitStatusComputer: HabitStatusComputer = HabitStatusComputerImpl()
    private val streakComputer: StreakComputer = StreakComputerImpl(habitStatusComputer)

    @Test
    fun `a series of completed dates forms a streak`() {
        val streakStart = LocalDate(2024, 1, 3)
        val streakEnd = LocalDate(2024, 1, 6)
        val today = LocalDate(2024, 1, 31)

        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = Schedule.EveryDaySchedule(startDate = streakStart),
        )
        val completionHistory = (streakStart..streakEnd).map { date ->
            Habit.YesNoHabit.CompletionRecord(
                date = date,
                completed = true,
            )
        }
        val vacationHistory = emptyList<Vacation>()

        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        assertThat(streaks).containsExactly(
            Streak(
                startDate = streakStart,
                endDate = streakEnd,
            )
        )
    }

    @Test
    fun `one completed date forms a streak`() {
        val completedDate = LocalDate(2024, 1, 3)
        val today = LocalDate(2024, 1, 31)

        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = Schedule.EveryDaySchedule(startDate = completedDate),
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            )
        )
        val vacationHistory = emptyList<Vacation>()

        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        assertThat(streaks).containsExactly(
            Streak(
                startDate = completedDate,
                endDate = completedDate,
            )
        )
    }

    @ParameterizedTest
    @CsvSource("2024-01-03", "2024-01-04", "2024-01-05", "2024-01-06", "2024-01-07")
    fun `not due dates near the completed date are included in the streak`(completedDateString: String) {
        val streakStart = LocalDate(2024, 1, 3)
        val streakEnd = LocalDate(2024, 1, 7)
        val today = LocalDate(2024, 1, 31)

        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            numOfDueDays = 3,
            startDate = LocalDate(2024, 1, 1),
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDateString.toLocalDate(),
                completed = true,
            )
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )

        assertThat(streaks).containsExactly(
            Streak(
                startDate = streakStart,
                endDate = streakEnd,
            )
        )
    }

    @Test
    fun `not due dates before the completed date are not included in the streak if backlog is disabled`() {
        val startDate = LocalDate(2024, 1, 1)
        val completedDate = LocalDate(2024, 1, 5)
        val today = LocalDate(2024, 1, 31)

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
            startDate = startDate,
            backlogEnabled = false,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            )
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )

        for (streak in streaks) {
            assertThat(streak.startDate..streak.endDate).containsNoneIn(
                startDate..completedDate.minus(DatePeriod(days = 1))
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        "false, 2024-01-05, 2024-01-10, 2024-01-31",
        "true, 2024-01-05, 2024-01-07, 2024-01-31",
        "false, 2024-01-05, 2024-01-10, 2024-01-10",
        "true, 2024-01-05, 2024-01-07, 2024-01-10"
    )
    fun `streak continues in future period`(
        periodSeparationEnabled: Boolean,
        streakStartDateString: String,
        streakEndDateString: String,
        todayString: String,
    ) {
        val startDate = LocalDate(2024, 1, 1)
        val completedDate = LocalDate(2024, 1, 5)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            startDate = startDate,
            periodSeparationEnabled = periodSeparationEnabled,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            )
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = todayString.toLocalDate(),
        )
        assertThat(streaks).containsExactly(
            Streak(
                startDate = streakStartDateString.toLocalDate(),
                endDate = streakEndDateString.toLocalDate(),
            )
        )
    }

    @Test
    fun `streaks do not continue after today`() {
        val startDate = LocalDate(2024, 1, 8)
        val completedDate = LocalDate(2024, 1, 12)
        val today = LocalDate(2024, 1, 13)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            )
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        for (streak in streaks) {
            assertThat(streak.endDate).isAtMost(today)
        }
    }

    @Test
    fun `today is included in streak when the habit is completed today`() {
        val startDate = LocalDate(2024, 1, 1)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val today = LocalDate(2024, 1, 12) // Friday
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = today.minus(DatePeriod(days = 1)),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = today,
                completed = true,
            ),
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        for (streak in streaks) {
            assertThat(streak.startDate..streak.endDate).contains(today)
        }
    }

    @Test
    fun `today is included in streak when the habit is not due today`() {
        val startDate = LocalDate(2024, 1, 1)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val today = LocalDate(2024, 1, 12) // Friday
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = today.minus(DatePeriod(days = 1)),
                completed = true,
            ),
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        for (streak in streaks) {
            assertThat(streak.startDate..streak.endDate).contains(today)
        }
    }

    @Test
    fun `today is not included in streak when the habit is due but not completed today`() {
        val startDate = LocalDate(2024, 1, 1)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val today = LocalDate(2024, 1, 12) // Friday
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = today.minus(DatePeriod(days = 1)),
                completed = true,
            ),
        )
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )
        for (streak in streaks) {
            assertThat(streak.startDate..streak.endDate).doesNotContain(today)
        }
    }

    @Test
    fun `returns corresponding streaks when only part of completion history is provided`() {
        val startDate = LocalDate(2023, 12, 1)
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(1, 2, 4, 5, 11, 12, 18, 19, 25, 26),
            startDate = startDate,
            periodSeparationEnabled = true,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val today = LocalDate(2024, 2, 2)
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 29),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 5),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 15),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 18),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 26),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 2, 2),
                completed = true,
            ),
        )
        val expectedStreakHistory = listOf(
            Streak(
                startDate = LocalDate(2023, 12, 26),
                endDate = LocalDate(2023, 12, 31),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 5),
                endDate = LocalDate(2024, 1, 10),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 12),
                endDate = LocalDate(2024, 1, 18),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 26),
                endDate = LocalDate(2024, 1, 31),
            ),
            Streak(
                startDate = LocalDate(2024, 2, 2),
                endDate = LocalDate(2024, 2, 2),
            ),
        )
        val expectedStreaks = expectedStreakHistory.filter {
            val firstStreakStartDate = LocalDate(2024, 1, 5)
            val lastStreakEndDate = LocalDate(2024, 1, 31)
            it.startDate >= firstStreakStartDate && it.endDate <= lastStreakEndDate
        }
        val vacationHistory = emptyList<Vacation>()
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory.filter {
                val start = LocalDate(2024, 1, 5)
                val end = LocalDate(2024, 1, 26)
                it.date in start..end
            },
            vacationHistory = vacationHistory,
            today = today,
        )
        assertThat(streaks).containsExactlyElementsIn(expectedStreaks)
    }

    @Test
    fun `vacation period is included in streak if streak lasted before vacation`() {
        val startDate = LocalDate(2024, 1, 1)
        val completedDate = LocalDate(2024, 1, 5)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            )
        )
        val vacationHistory = listOf(
            Vacation(
                startDate = LocalDate(2024, 1, 5),
                endDate = LocalDate(2024, 1, 5),
            )
        )
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = LocalDate(2024, 1, 31),
        )
        assertThat(streaks).containsExactly(
            Streak(
                startDate = startDate,
                endDate = startDate.plusDays(6),
            )
        )
    }

    @Test
    fun `vacation period is not included in streak if streak did not last before vacation`() {
        val startDate = LocalDate(2024, 1, 1)
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            startDate = startDate,
        )
        val habit = Habit.YesNoHabit(
            name = "test habit",
            schedule = schedule,
        )
        val completionHistory = emptyList<Habit.YesNoHabit.CompletionRecord>()
        val vacationHistory = listOf(
            Vacation(
                startDate = LocalDate(2024, 1, 5),
                endDate = LocalDate(2024, 1, 5),
            )
        )
        val streaks = streakComputer.computeAllStreaks(
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = LocalDate(2024, 1, 31),
        )
        assertThat(streaks).isEmpty()
    }
}