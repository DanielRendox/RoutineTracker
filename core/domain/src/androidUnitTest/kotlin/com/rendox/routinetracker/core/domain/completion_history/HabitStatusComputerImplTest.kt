package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class HabitStatusComputerImplTest {
    private val defaultSchedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
        dueDaysOfWeek = listOf(
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SUNDAY,
        ),
        startDayOfWeek = DayOfWeek.MONDAY,
        backlogEnabled = true,
        completingAheadEnabled = true,
        periodSeparationEnabled = false,
        startDate = LocalDate(2023, 12, 4),
    )

    private val defaultHabit = Habit.YesNoHabit(
        id = 1L,
        name = "",
        schedule = defaultSchedule,
    )

    private val habitStatusComputer: HabitStatusComputer = HabitStatusComputerImpl()

    @ParameterizedTest(name = "{0}")
    @MethodSource("computeStatusDataProvider")
    fun computeStatusTest(
        testName: String,
        validationDate: LocalDate,
        today: LocalDate,
        numOfTimesCompleted: Float,
        completedDates: Array<LocalDate>,
        expectedStatus: HabitStatus,
    ) {
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = validationDate,
            today = today,
            habit = defaultHabit,
            completionHistory = completedDates.map { date ->
                Habit.YesNoHabit.CompletionRecord(
                    date = date,
                    numOfTimesCompleted = numOfTimesCompleted,
                )
            },
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(expectedStatus)
    }

    @Test
    fun `future date, backlog, but backlog is disabled, not completed, assert status is NotDue`() {
        val schedule = defaultSchedule.copy(backlogEnabled = false)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = LocalDate(2023, 12, 11),
            today = LocalDate(2023, 12, 10),
            habit = habit,
            completionHistory = emptyList(),
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
    }

    @Test
    fun `future date, backlog, but period separation enabled, another period, not completed, assert status is NotDue`() {
        val schedule =
            defaultSchedule.copy(periodSeparationEnabled = true, backlogEnabled = true)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = LocalDate(2023, 12, 11),
            today = LocalDate(2023, 12, 10),
            habit = habit,
            completionHistory = emptyList(),
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
    }

    @Test
    fun `future date, already completed, but completing ahead disabled, assert status is Planned`() {
        val schedule = defaultSchedule.copy(completingAheadEnabled = false)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = LocalDate(2023, 12, 13),
            today = LocalDate(2023, 12, 9),
            habit = habit,
            completionHistory = (2..5).map {
                Habit.YesNoHabit.CompletionRecord(
                    date = defaultSchedule.startDate.plusDays(it),
                )
            },
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Planned)
    }

    @Test
    fun `future date, already completed, but period separation enabled, assert status is Planned`() {
        val schedule =
            defaultSchedule.copy(periodSeparationEnabled = true, completingAheadEnabled = true)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = LocalDate(2023, 12, 13),
            today = LocalDate(2023, 12, 9),
            habit = habit,
            completionHistory = (2..5).map {
                Habit.YesNoHabit.CompletionRecord(
                    date = defaultSchedule.startDate.plusDays(it),
                )
            },
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Planned)
    }

    @Test
    fun `due, completed, assert status is PartiallyCompleted no matter the date`() {
        val lastDueDate = LocalDate(2023, 12, 27)
        val dueDates = mutableListOf<LocalDate>()
        for (date in defaultSchedule.startDate..lastDueDate) {
            if (defaultSchedule.isDue(validationDate = date)) dueDates.add(date)
        }
        for (validationDate in dueDates) {
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = validationDate,
                today = LocalDate(2023, 12, 14),
                habit = defaultHabit,
                completionHistory = dueDates.map {
                    Habit.YesNoHabit.CompletionRecord(
                        date = it,
                        numOfTimesCompleted = 0.5F,
                    )
                },
                vacationHistory = emptyList(),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.PartiallyCompleted)
        }
    }

    @Test
    fun `future date, due, backlog, completed more times than planned, assert status is SortedOutBacklog`() {
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = LocalDate(2023, 12, 7), // Wednesday
            today = LocalDate(2023, 12, 7),
            habit = defaultHabit,
            completionHistory = listOf(
                Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 7), // Wednesday
                    numOfTimesCompleted = 2F,
                )
            ),
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.SortedOutBacklog)
    }

    @Test
    fun `on vacation, not completed, assert status is OnVacation no matter the date`() {
        val vacationStartDate = LocalDate(2023, 12, 4)
        val vacationEndDate = LocalDate(2023, 12, 10)
        for (date in vacationStartDate..vacationEndDate) {
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = date,
                today = LocalDate(2023, 12, 7),
                habit = defaultHabit,
                completionHistory = emptyList(),
                vacationHistory = listOf(
                    Vacation(
                        startDate = vacationStartDate,
                        endDate = vacationEndDate,
                    )
                ),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.OnVacation)
        }
    }

    @Test
    fun `on vacation, no backlog, completed, assert status is OverCompleted`() {
        val vacationStartDate = LocalDate(2023, 12, 4)
        val vacationEndDate = LocalDate(2023, 12, 10)

        val habitStatusComputer = HabitStatusComputerImpl(

        )

        for (currentDate in vacationStartDate..vacationEndDate) {
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = currentDate,
                today = LocalDate(2023, 12, 13),
                habit = defaultHabit,
                completionHistory = (vacationStartDate..vacationEndDate).map { date ->
                    Habit.YesNoHabit.CompletionRecord(date = date)
                },
                vacationHistory = listOf(
                    Vacation(
                        startDate = vacationStartDate,
                        endDate = vacationEndDate,
                    )
                ),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.OverCompleted)
        }
    }

    @Test
    fun `on vacation, backlog, completed, assert only necessary dates have SortedOutBacklog status`() {
        val vacationStartDate = LocalDate(2023, 12, 11)
        val vacationEndDate = LocalDate(2023, 12, 17)
        val today = LocalDate(2023, 12, 17)

        val resultingStatuses = mutableListOf<HabitStatus>()
        val expectedStatuses = mutableListOf<HabitStatus>()

        for (currentDate in vacationStartDate..vacationEndDate) {
            if (currentDate <= LocalDate(2023, 12, 13)) {
                expectedStatuses.add(HabitStatus.SortedOutBacklog)
            } else {
                expectedStatuses.add(HabitStatus.OverCompleted)
            }

            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = currentDate,
                today = today,
                habit = defaultHabit,
                completionHistory = (vacationStartDate..vacationEndDate).map { date ->
                    Habit.YesNoHabit.CompletionRecord(date = date)
                },
                vacationHistory = listOf(
                    Vacation(
                        startDate = vacationStartDate,
                        endDate = vacationEndDate,
                    )
                ),
            )
            resultingStatuses.add(habitStatus)
        }

        assertThat(resultingStatuses).containsExactlyElementsIn(expectedStatuses).inOrder()
    }

    @Test
    fun `date after habit end date, assert status is Finished`() = runTest {
        val endDate = LocalDate(2023, 12, 31)
        val schedule = defaultSchedule.copy(endDate = endDate)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = endDate.plusDays(1),
            today = schedule.startDate,
            habit = habit,
            completionHistory = emptyList(),
            vacationHistory = emptyList(),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Finished)
    }

    @ParameterizedTest
    @CsvSource("2023-12-09", "2023-12-10")
    fun `backlog disabled, negative schedule deviation, assert over completing completes ahead`(
        validationDateString: String
    ) {
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
            backlogEnabled = false,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2023, 12, 4),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        assertThat(
            habitStatusComputer.computeStatus(
                validationDate = validationDateString.toLocalDate(),
                today = LocalDate(2024, 1, 11),
                habit = habit,
                completionHistory = listOf(
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2023, 12, 6),
                        numOfTimesCompleted = 1F,
                    ),
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2023, 12, 8),
                        numOfTimesCompleted = 1F,
                    ),
                ),
                vacationHistory = emptyList(),
            )
        ).isEqualTo(HabitStatus.PastDateAlreadyCompleted)
    }

    @ParameterizedTest
    @CsvSource("2024-01-08, CompletedLater", "2024-01-12, NotDue")
    fun `assert sorting out backlog in the past revokes previous failed status and future backlog`(
        validationDateString: String,
        expectedStatus: HabitStatus,
    ) {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1, 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        assertThat(
            habitStatusComputer.computeStatus(
                validationDate = validationDateString.toLocalDate(),
                today = LocalDate(2024, 1, 11),
                habit = habit,
                completionHistory = listOf(
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2024, 1, 10),
                        numOfTimesCompleted = 1F,
                    )
                ),
                vacationHistory = emptyList(),
            )
        ).isEqualTo(expectedStatus)
    }

    @Test
    fun `assert sorting out backlog in the future does not revoke future backlog`() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1, 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        assertThat(
            habitStatusComputer.computeStatus(
                validationDate = LocalDate(2024, 1, 12),
                today = LocalDate(2024, 1, 11),
                habit = habit,
                completionHistory = listOf(
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2024, 1, 11),
                        numOfTimesCompleted = 1F,
                    )
                ),
                vacationHistory = emptyList(),
            )
        ).isEqualTo(HabitStatus.Backlog)
    }

    @Test
    fun `assert sorting out backlog does not transform partially completed date to completed later`() {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1, 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        assertThat(
            habitStatusComputer.computeStatus(
                validationDate = LocalDate(2024, 1, 8),
                today = LocalDate(2024, 1, 11),
                habit = habit,
                completionHistory = listOf(
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2024, 1, 8),
                        numOfTimesCompleted = 0.5F,
                    ),
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2024, 1, 10),
                        numOfTimesCompleted = 1F,
                    ),
                ),
                vacationHistory = emptyList(),
            )
        ).isEqualTo(HabitStatus.PartiallyCompleted) // not CompletedLater
    }

    @Test
    fun `assert partial completing introduces backlog`() {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1, 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        assertThat(
            habitStatusComputer.computeStatus(
                validationDate = LocalDate(2024, 1, 11),
                today = LocalDate(2024, 1, 11),
                habit = habit,
                completionHistory = listOf(
                    Habit.YesNoHabit.CompletionRecord(
                        date = LocalDate(2024, 1, 8),
                        numOfTimesCompleted = 0.5F,
                    )
                ),
                vacationHistory = emptyList(),
            )
        ).isEqualTo(HabitStatus.Backlog)
    }

    @Test
    fun `bug, assert completion today completes ahead`() {
        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = listOf(
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2023, 1, 30),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        val completionHistory = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 30),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 31),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 2, 1),
                completed = true,
            ),
        )
        val assertionDates =
            LocalDate(2024, 2, 2)..LocalDate(2024, 2, 3)
        val resultingStatuses = assertionDates.map { date ->
            habitStatusComputer.computeStatus(
                validationDate = date,
                today = LocalDate(2024, 2, 1),
                habit = habit,
                completionHistory = completionHistory,
                vacationHistory = emptyList(),
            )
        }
        val expectedStatuses = listOf(
            HabitStatus.FutureDateAlreadyCompleted,
            HabitStatus.FutureDateAlreadyCompleted,
        )
        assertThat(resultingStatuses).containsExactlyElementsIn(expectedStatuses)
    }

    companion object {
        @JvmStatic
        fun computeStatusDataProvider() = arrayOf(
            arrayOf(
                "future date, due, not completed, assert status is Planned",
                LocalDate(2023, 12, 14), // Thursday
                LocalDate(2023, 12, 4), // Monday
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.Planned,
            ),
            arrayOf(
                "due, completed, assert status is Completed",
                LocalDate(2023, 12, 27),
                LocalDate(2023, 12, 14),
                1F,
                arrayOf(LocalDate(2023, 12, 27)),
                HabitStatus.Completed,
            ),
            arrayOf(
                "due, completed more times than planned, assert status is OverCompleted",
                LocalDate(2023, 12, 6),
                LocalDate(2023, 12, 14),
                2F,
                arrayOf(LocalDate(2023, 12, 6)),
                HabitStatus.OverCompleted,
            ),
            arrayOf(
                "future date, not due, not completed, assert status is NotDue",
                LocalDate(2023, 12, 8), // Friday,
                LocalDate(2023, 12, 4),
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.NotDue,
            ),
            arrayOf(
                "future date, backlog, not completed, assert status is Backlog",
                LocalDate(2023, 12, 12), // Tuesday
                LocalDate(2023, 12, 11),
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.Backlog,
            ),
            arrayOf(
                "completed on time, not due, not completed, assert status is NotDue",
                LocalDate(2023, 12, 12),
                LocalDate(2023, 12, 11),
                1F,
                arrayOf(
                    LocalDate(2023, 12, 6),
                    LocalDate(2023, 12, 7),
                    LocalDate(2023, 12, 10),
                ),
                HabitStatus.NotDue,
            ),
            arrayOf(
                "future date, already completed, assert status is FutureDateAlreadyCompleted",
                LocalDate(2023, 12, 13),
                LocalDate(2023, 12, 9),
                1F,
                arrayOf(
                    LocalDate(2023, 12, 6),
                    LocalDate(2023, 12, 7),
                    LocalDate(2023, 12, 8),
                    LocalDate(2023, 12, 9),
                ),
                HabitStatus.FutureDateAlreadyCompleted,
            ),
            arrayOf(
                "past date, due, not completed, assert status is Failed",
                LocalDate(2023, 12, 13),
                LocalDate(2023, 12, 24),
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.Failed,
            ),
            arrayOf(
                "past date, due, completed later, assert status is CompletedLater",
                LocalDate(2023, 12, 7),
                LocalDate(2023, 12, 24),
                1F,
                arrayOf(LocalDate(2023, 12, 8)),
                HabitStatus.CompletedLater,
            ),
            arrayOf(
                "past date, due, completed later but not compensated for current date, assert status is Failed",
                LocalDate(2023, 12, 6),
                LocalDate(2023, 12, 24),
                1F,
                arrayOf(LocalDate(2023, 12, 8)),
                HabitStatus.Failed,
            ),
            arrayOf(
                "past date, due, compensated later for two days, assert statuses of first is CompletedLater",
                LocalDate(2023, 12, 6),
                LocalDate(2023, 12, 24),
                2F,
                arrayOf(LocalDate(2023, 12, 8)),
                HabitStatus.CompletedLater,
            ),
            arrayOf(
                "past date, due, compensated later for two days, assert statuses of second are is CompletedLater",
                LocalDate(2023, 12, 7),
                LocalDate(2023, 12, 24),
                2F,
                arrayOf(LocalDate(2023, 12, 8)),
                HabitStatus.CompletedLater,
            ),
            arrayOf(
                "past date, due, completed previously but not compensated for current date, assert status is Failed",
                LocalDate(2023, 12, 10),
                LocalDate(2023, 12, 24),
                1F,
                arrayOf(LocalDate(2023, 12, 8)),
                HabitStatus.Failed,
            ),
            arrayOf(
                "past date, completed both earlier and later, assert status is AlreadyCompleted",
                LocalDate(2023, 12, 10),
                LocalDate(2023, 12, 11),
                1F,
                arrayOf(
                    LocalDate(2023, 12, 6),
                    LocalDate(2023, 12, 7),
                    LocalDate(2023, 12, 8),
                    LocalDate(2023, 12, 11),
                ),
                HabitStatus.PastDateAlreadyCompleted,
            ),
            arrayOf(
                "over completed and sorted out backlog on 2023-12-13, assert CompletedLater on 2023-12-06",
                LocalDate(2023, 12, 6),
                LocalDate(2023, 12, 18),
                5F,
                arrayOf(LocalDate(2023, 12, 13)),
                HabitStatus.CompletedLater,
            ),
            arrayOf(
                "over completed and sorted out backlog on the same date, assert previous date's status is SortedOutBacklog",
                LocalDate(2023, 12, 7),
                LocalDate(2023, 12, 18),
                5F,
                arrayOf(LocalDate(2023, 12, 13)),
                HabitStatus.CompletedLater,
            ),
            arrayOf(
                "over completed and sorted out backlog on the same date, assert its status is SortedOutBacklog",
                LocalDate(2023, 12, 13),
                LocalDate(2023, 12, 18),
                5F,
                arrayOf(LocalDate(2023, 12, 13)),
                HabitStatus.SortedOutBacklog,
            ),
            arrayOf(
                "over completed and sorted out backlog on the same date, assert next date's status is AlreadyCompleted",
                LocalDate(2023, 12, 14),
                LocalDate(2023, 12, 18),
                5F,
                arrayOf(LocalDate(2023, 12, 13)),
                HabitStatus.PastDateAlreadyCompleted,
            ),
            arrayOf(
                "date before habit start date, assert status is NotStarted",
                LocalDate(2023, 12, 3),
                LocalDate(2023, 12, 3),
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.NotStarted,
            ),
            arrayOf(
                "past date, not due, not completed, no backlog, assert status is Skipped",
                LocalDate(2023, 12, 5),
                LocalDate(2024, 1, 1),
                0F,
                emptyArray<LocalDate>(),
                HabitStatus.Skipped,
            ),
        )
    }
}