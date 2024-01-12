package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Vacation
import com.rendox.routinetracker.core.testcommon.fakes.habit.CompletionHistoryRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitData
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.VacationRepositoryFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Before
import org.junit.Test

class HabitComputeStatusUseCaseTest {
    private lateinit var completionHistoryRepository: CompletionHistoryRepository
    private lateinit var computeStatus: HabitComputeStatusUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var vacationRepository: VacationRepository

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

    @Before
    fun setUp() = runTest {
        val habitData = HabitData()
        habitRepository = HabitRepositoryFake(habitData)
        vacationRepository = VacationRepositoryFake(habitData)
        completionHistoryRepository = CompletionHistoryRepositoryFake(habitData)

        habitRepository.insertHabit(defaultHabit)

        computeStatus = HabitComputeStatusUseCase(
            habitRepository = habitRepository,
            completionHistoryRepository = completionHistoryRepository,
            vacationRepository = vacationRepository,
        )
    }

    @Test
    fun `future date, due, not completed, assert status is Planned`() = runTest {
        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 14), // Thursday
            today = defaultSchedule.startDate,
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Planned)
    }

    @Test
    fun `due, completed, assert status is Completed no matter the date`() = runTest {
        val lastDueDate = LocalDate(2023, 12, 27)
        val dueDates = mutableListOf<LocalDate>()
        for (date in defaultSchedule.startDate..lastDueDate) {
            if (defaultSchedule.isDue(validationDate = date)) dueDates.add(date)
        }
        for (validationDate in dueDates) {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(date = validationDate),
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = validationDate,
                today = LocalDate(2023, 12, 14),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Completed)
        }
    }

    @Test
    fun `due, completed, assert status is PartiallyCompleted no matter the date`() = runTest {
        val lastDueDate = LocalDate(2023, 12, 27)
        val dueDates = mutableListOf<LocalDate>()
        for (date in defaultSchedule.startDate..lastDueDate) {
            if (defaultSchedule.isDue(validationDate = date)) dueDates.add(date)
        }
        for (validationDate in dueDates) {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = validationDate,
                    numOfTimesCompleted = 0.5F,
                ),
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = validationDate,
                today = LocalDate(2023, 12, 14),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.PartiallyCompleted)
        }
    }

    @Test
    fun `due, completed more times than planned, assert status is OverCompleted no matter the date`() =
        runTest {
            val lastDueDate = LocalDate(2023, 12, 27)
            val dueDates =
                getDueDatesInPeriod(defaultSchedule.startDate..lastDueDate, defaultSchedule)

            for (validationDate in dueDates) {
                completionHistoryRepository.insertCompletion(
                    habitId = 1L,
                    completionRecord = Habit.YesNoHabit.CompletionRecord(
                        date = validationDate,
                        numOfTimesCompleted = 2F,
                    ),
                )

                val habitStatus = computeStatus(
                    habitId = 1L,
                    validationDate = validationDate,
                    today = LocalDate(2023, 12, 14),
                )
                assertThat(habitStatus).isEqualTo(HabitStatus.OverCompleted)
            }
        }

    @Test
    fun `future date, due, backlog, completed more times than planned, assert status is SortedOutBacklog`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 7), // Wednesday
                    numOfTimesCompleted = 2F,
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 7), // Wednesday
                today = LocalDate(2023, 12, 7),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.SortedOutBacklog)
        }

    @Test
    fun `future date, not due, backlog, completed, assert status is SortedOutBacklog`() = runTest {
        completionHistoryRepository.insertCompletion(
            habitId = 1L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 7), // Wednesday
                numOfTimesCompleted = 2F,
            ),
        )

        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 7), // Wednesday
            today = LocalDate(2023, 12, 7),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.SortedOutBacklog)
    }

    @Test
    fun `future date, not due, not completed, assert status is NotDue`() = runTest {
        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 8), // Friday
            today = defaultSchedule.startDate,
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
    }

    @Test
    fun `future date, backlog, not completed, assert status is Backlog`() = runTest {
        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 12), // Tuesday
            today = LocalDate(2023, 12, 11),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Backlog)
    }

    @Test
    fun `completed on time, not due, not completed, assert status is NotDue`() = runTest {
        completionHistoryRepository.insertCompletion(
            habitId = 1L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 6), // Wednesday
            )

        )
        completionHistoryRepository.insertCompletion(
            habitId = 1L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 7), // Thursday
            )
        )
        completionHistoryRepository.insertCompletion(
            habitId = 1L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 10), // Sunday
            )
        )

        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 12),
            today = LocalDate(2023, 12, 11),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
    }

    @Test
    fun `future date, backlog, but backlog is disabled, not completed, assert status is NotDue`() =
        runTest {
            val schedule = defaultSchedule.copy(backlogEnabled = false)
            val habit = defaultHabit.copy(id = 2L, schedule = schedule)
            habitRepository.insertHabit(habit)

            val habitStatus = computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 11),
                today = LocalDate(2023, 12, 10),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
        }

    @Test
    fun `future date, backlog, but period separation enabled, another period, not completed, assert status is NotDue`() =
        runTest {
            val schedule =
                defaultSchedule.copy(periodSeparationEnabled = true, backlogEnabled = true)
            val habit = defaultHabit.copy(id = 2L, schedule = schedule)
            habitRepository.insertHabit(habit)

            val habitStatus = computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 11),
                today = LocalDate(2023, 12, 10),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.NotDue)
        }

    @Test
    fun `future date, already completed, assert status is FutureDateAlreadyCompleted`() = runTest {
        for (dayIndex in 2..5) {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = defaultSchedule.startDate.plusDays(dayIndex),
                )
            )
        }

        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 13),
            today = LocalDate(2023, 12, 9),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.FutureDateAlreadyCompleted)
    }

    @Test
    fun `future date, already completed, but completing ahead disabled, assert status is Planned`() =
        runTest {
            val schedule = defaultSchedule.copy(completingAheadEnabled = false)
            val habit = defaultHabit.copy(id = 2L, schedule = schedule)
            habitRepository.insertHabit(habit)

            for (dayIndex in 2..5) {
                completionHistoryRepository.insertCompletion(
                    habitId = 2L,
                    completionRecord = Habit.YesNoHabit.CompletionRecord(
                        date = defaultSchedule.startDate.plusDays(dayIndex), // Wednesday
                    )
                )
            }

            val habitStatus = computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 13),
                today = LocalDate(2023, 12, 9),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Planned)
        }

    @Test
    fun `future date, already completed, but period separation enabled, assert status is Planned`() =
        runTest {
            val schedule =
                defaultSchedule.copy(periodSeparationEnabled = true, completingAheadEnabled = true)
            val habit = defaultHabit.copy(id = 2L, schedule = schedule)
            habitRepository.insertHabit(habit)

            for (dayIndex in 2..5) {
                completionHistoryRepository.insertCompletion(
                    habitId = 2L,
                    completionRecord = Habit.YesNoHabit.CompletionRecord(
                        date = defaultSchedule.startDate.plusDays(dayIndex), // Wednesday
                    )
                )
            }

            val habitStatus = computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 13),
                today = LocalDate(2023, 12, 9),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Planned)
        }

    @Test
    fun `past date, due, not completed, assert status is Failed`() = runTest {
        val today = LocalDate(2023, 12, 24)
        val dueDates = getDueDatesInPeriod(defaultSchedule.startDate..today, defaultSchedule)

        for (validationDate in dueDates) {
            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 13), // Tuesday
                today = today,
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Failed)
        }
    }

    @Test
    fun `past date, due, completed later, assert status is CompletedLater`() = runTest {
        completionHistoryRepository.insertCompletion(
            habitId = 1L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 8), // Friday
            )
        )

        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 7), // Thursday
            today = LocalDate(2023, 12, 24),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.CompletedLater)
    }

    @Test
    fun `past date, due, completed later but not compensated for current date, assert status is Failed`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 8), // Friday
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 6), // Wednesday
                today = LocalDate(2023, 12, 24),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Failed)
        }

    @Test
    fun `past date, due, compensated later for two days, assert statuses of both are is CompletedLater`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 8), // Friday
                    numOfTimesCompleted = 2F,
                )
            )

            val statusOnWednesday = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 6), // Wednesday
                today = LocalDate(2023, 12, 24),
            )
            assertThat(statusOnWednesday).isEqualTo(HabitStatus.CompletedLater)

            val statusOnThursday = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 7), // Thursday
                today = LocalDate(2023, 12, 24),
            )
            assertThat(statusOnThursday).isEqualTo(HabitStatus.CompletedLater)
        }

    @Test
    fun `past date, due, completed previously but not compensated for current date, assert status is Failed`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 8), // Friday
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 10), // Sunday
                today = LocalDate(2023, 12, 24),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.Failed)
        }

    @Test
    fun `past date, completed both earlier and later, assert status is AlreadyCompleted`() =
        runTest {
            val startDate = LocalDate(2023, 12, 6)
            val endDate = LocalDate(2023, 12, 8)

            for (date in startDate..endDate) {
                completionHistoryRepository.insertCompletion(
                    habitId = 1L,
                    completionRecord = Habit.YesNoHabit.CompletionRecord(
                        date = date,
                    )
                )
            }
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 11),
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 10),
                today = LocalDate(2023, 12, 11),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.PastDateAlreadyCompleted)
        }

    @Test
    fun `on vacation, not completed, assert status is OnVacation no matter the date`() = runTest {
        val vacationStartDate = LocalDate(2023, 12, 4)
        val vacationEndDate = LocalDate(2023, 12, 10)

        vacationRepository.insertVacation(
            habitId = 1L,
            vacation = Vacation(
                startDate = vacationStartDate,
                endDate = vacationEndDate,
            ),
        )

        for (date in vacationStartDate..vacationEndDate) {
            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = date,
                today = LocalDate(2023, 12, 7),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.OnVacation)
        }
    }

    @Test
    fun `on vacation, no backlog, completed, assert status is OverCompleted`() = runTest {
        val vacationStartDate = LocalDate(2023, 12, 4)
        val vacationEndDate = LocalDate(2023, 12, 10)

        vacationRepository.insertVacation(
            habitId = 1L,
            vacation = Vacation(
                startDate = vacationStartDate,
                endDate = vacationEndDate,
            ),
        )

        for (date in vacationStartDate..vacationEndDate) {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(date = date)
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = date,
                today = LocalDate(2023, 12, 13),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.OverCompleted)
        }
    }

    @Test
    fun `on vacation, backlog, completed, assert only necessary dates have SortedOutBacklog status`() =
        runTest {
            val vacationStartDate = LocalDate(2023, 12, 11)
            val vacationEndDate = LocalDate(2023, 12, 17)

            vacationRepository.insertVacation(
                habitId = 1L,
                vacation = Vacation(
                    startDate = vacationStartDate,
                    endDate = vacationEndDate,
                ),
            )

            val today = LocalDate(2023, 12, 17)

            val resultingStatuses = mutableListOf<HabitStatus>()
            val expectedStatuses = mutableListOf<HabitStatus>()

            for (date in vacationStartDate..vacationEndDate) {
                if (date <= LocalDate(2023, 12, 13)) {
                    expectedStatuses.add(HabitStatus.SortedOutBacklog)
                } else {
                    expectedStatuses.add(HabitStatus.OverCompleted)
                }

                completionHistoryRepository.insertCompletion(
                    habitId = 1L,
                    completionRecord = Habit.YesNoHabit.CompletionRecord(date = date)
                )

                val habitStatus = computeStatus(
                    habitId = 1L,
                    validationDate = date,
                    today = today,
                )
                resultingStatuses.add(habitStatus)
            }

            assertThat(resultingStatuses).containsExactlyElementsIn(expectedStatuses).inOrder()
        }

    @Test
    fun `over completed and sorted out backlog at the same time, assert previous days have CompletedLater statuses`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 13),
                    numOfTimesCompleted = 5F,
                )
            )

            val previousDueDates = listOf(
                LocalDate(2023, 12, 6),
                LocalDate(2023, 12, 7),
                LocalDate(2023, 12, 10),
            )

            val expectedStatuses =
                previousDueDates.associateWith { HabitStatus.CompletedLater }
            val resultingStatuses = previousDueDates.associateWith {
                computeStatus(
                    habitId = 1L,
                    validationDate = it,
                    today = LocalDate(2023, 12, 18),
                )
            }

            assertThat(resultingStatuses).containsExactlyEntriesIn(expectedStatuses).inOrder()
        }

    @Test
    fun `over completed and sorted out backlog at the same time, assert next days have AlreadyCompleted status`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 13),
                    numOfTimesCompleted = 5F,
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 14),
                today = LocalDate(2023, 12, 18),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.PastDateAlreadyCompleted)
        }

    @Test
    fun `over completed and sorted out backlog at the same time, assert status is SortedOutBacklog`() =
        runTest {
            completionHistoryRepository.insertCompletion(
                habitId = 1L,
                completionRecord = Habit.YesNoHabit.CompletionRecord(
                    date = LocalDate(2023, 12, 13),
                    numOfTimesCompleted = 5F,
                )
            )

            val habitStatus = computeStatus(
                habitId = 1L,
                validationDate = LocalDate(2023, 12, 13),
                today = LocalDate(2023, 12, 18),
            )
            assertThat(habitStatus).isEqualTo(HabitStatus.SortedOutBacklog)
        }

    @Test
    fun `date before habit start date, assert status is NotStarted`() = runTest {
        val dateBeforeStartDate = defaultSchedule.startDate.minus(DatePeriod(days = 1))
        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = dateBeforeStartDate,
            today = dateBeforeStartDate,
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.NotStarted)
    }

    @Test
    fun `date after habit end date, assert status is Finished`() = runTest {
        val endDate = LocalDate(2023, 12, 31)
        val schedule = defaultSchedule.copy(endDate = endDate)
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        val habitStatus = computeStatus(
            habitId = 2L,
            validationDate = endDate.plusDays(1),
            today = schedule.startDate,
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Finished)
    }

    @Test
    fun `past date, not due, not completed, no backlog, assert status is Skipped`() = runTest {
        val habitStatus = computeStatus(
            habitId = 1L,
            validationDate = LocalDate(2023, 12, 5), // Tuesday
            today = LocalDate(2024, 1, 1),
        )
        assertThat(habitStatus).isEqualTo(HabitStatus.Skipped)
    }

    @Test
    fun `backlog disabled, negative schedule deviation, assert over completing completes ahead`() = runTest {
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
            startDate = LocalDate(2023, 12 , 4),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 6),
                numOfTimesCompleted = 1F,
            )
        )
        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2023, 12, 8),
                numOfTimesCompleted = 1F,
            )
        )

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 9),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.PastDateAlreadyCompleted)

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2023, 12, 10),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.PastDateAlreadyCompleted)
    }

    @Test
    fun `assert sorting out backlog in the past revokes both previous failed status and future backlog`() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1 , 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 10),
                numOfTimesCompleted = 1F,
            )
        )

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2024, 1, 8),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.CompletedLater) // instead of Failed

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2024, 1, 12),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.NotDue) // instead of Backlog
    }

    @Test
    fun `assert sorting out backlog in the future does not revoke future backlog`() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1 , 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 11),
                numOfTimesCompleted = 1F,
            )
        )

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2024, 1, 12),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.Backlog)
    }

    @Test
    fun `assert sorting out backlog does not transform partially completed date to completed later`() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1 , 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 8),
                numOfTimesCompleted = 0.5F,
            )
        )

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 10),
                numOfTimesCompleted = 1F,
            )
        )

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2024, 1, 8),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.PartiallyCompleted) // not CompletedLater
    }

    @Test
    fun `assert partial completing introduces backlog`() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(2, 8),
            backlogEnabled = true,
            completingAheadEnabled = true,
            periodSeparationEnabled = true,
            startDate = LocalDate(2024, 1 , 1),
            weekDaysMonthRelated = emptyList(),
        )
        val habit = defaultHabit.copy(id = 2L, schedule = schedule)
        habitRepository.insertHabit(habit)

        completionHistoryRepository.insertCompletion(
            habitId = 2L,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 1, 8),
                numOfTimesCompleted = 0.5F,
            )
        )

        assertThat(
            computeStatus(
                habitId = 2L,
                validationDate = LocalDate(2024, 1, 11),
                today = LocalDate(2024, 1, 11),
            )
        ).isEqualTo(HabitStatus.Backlog)
    }

    companion object {
        private fun getDueDatesInPeriod(
            period: LocalDateRange,
            schedule: Schedule,
        ): List<LocalDate> {
            val dueDates = mutableListOf<LocalDate>()
            for (date in period) {
                if (schedule.isDue(validationDate = date)) dueDates.add(date)
            }
            return dueDates
        }
    }
}