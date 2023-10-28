package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepositoryImpl
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepositoryImpl
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.testcommon.fakes.routine.CompletionHistoryLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineData
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineLocalDataSourceFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Test
import kotlin.test.assertFailsWith

class InsertRoutineStatusIntoHistoryUseCaseTest {

    private val routineData = RoutineData()
    private val routineLocalDataSource = RoutineLocalDataSourceFake(routineData)
    private val completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceFake(routineData)
    private val completionHistoryRepository: CompletionHistoryRepository =
        CompletionHistoryRepositoryImpl(
            localDataSource = completionHistoryLocalDataSource,
        )
    private val routineRepository: RoutineRepository = RoutineRepositoryImpl(
        localDataSource = routineLocalDataSource,
    )
    private val insertRoutineStatusIntoHistory = InsertRoutineStatusIntoHistoryUseCase(
        completionHistoryRepository = completionHistoryRepository,
        routineRepository = routineRepository,
    )

    @Test
    fun `every day schedule, standard check`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 1)

        val routineEndDate = routineStartDate.plusDays(5)

        val schedule = Schedule.EveryDaySchedule(
            routineStartDate = routineStartDate,
            vacationStartDate = routineStartDate.plusDays(3),
            vacationEndDate = routineStartDate.plusDays(3),
            routineEndDate = routineEndDate,
        )

        val expectedHistory = listOf(
            HistoricalStatus.Completed,
            HistoricalStatus.Completed,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.OnVacation,
            HistoricalStatus.Completed,
            HistoricalStatus.NotCompleted,
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(index), status)
        }

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[0].date,
            completedOnCurrentDate = true,
        )
        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[1].date,
            completedOnCurrentDate = true,
        )
        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[2].date,
            completedOnCurrentDate = false,
        )
        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[3].date,
            completedOnCurrentDate = false,
        )
        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[4].date,
            completedOnCurrentDate = true,
        )
        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = expectedHistory[5].date,
            completedOnCurrentDate = false,
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(expectedHistory)

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = routineEndDate)
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = 0, schedule = expectedResultingSchedule)
        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)

        assertFailsWith<NullPointerException> {
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.minus(DatePeriod(days = 1)),
                completedOnCurrentDate = false,
            )
        }

        assertFailsWith<NullPointerException> {
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineEndDate.plus(DatePeriod(days = 1)),
                completedOnCurrentDate = false,
            )
        }
    }

    @Test
    fun `weekly schedule, due on explicit days of week, periodic separation enabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            periodSeparationEnabled = true,
            dueDaysOfWeek = listOf(
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            false,  // Thursday
            false,  // Friday       + backlog
            false,  // Saturday     + backlog
            true,   // Sunday       - backlog

            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            true,   // Thursday     - backlog
            false,  // Friday       + backlog
            true,   // Saturday
            true,   // Sunday       - backlog

            false,  // Monday
            false,  // Tuesday      + backlog
        )

        val firstSixDaysOfFirstWeek = listOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
            HistoricalStatus.Completed,
            HistoricalStatus.Skipped,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.NotCompleted,
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(index), status)
        }

        completionHistory.slice(0..5).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstSixDaysOfFirstWeek.first().date..firstSixDaysOfFirstWeek.last().date
            )
        ).isEqualTo(firstSixDaysOfFirstWeek)

        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = routineStartDate.plusDays(6),
            completedOnCurrentDate = completionHistory[6],
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                routineStartDate.plusDays(6), HistoricalStatus.SortedOutBacklog
            )
        )
        firstWeek[5] = firstWeek[5].copy(status = HistoricalStatus.CompletedLater)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val secondWeek = listOf(
            HistoricalStatus.Skipped,           // Monday
            HistoricalStatus.Completed,         // Tuesday
            HistoricalStatus.Completed,         // Wednesday
            HistoricalStatus.OverCompleted,     // Thursday
            HistoricalStatus.AlreadyCompleted,  // Friday
            HistoricalStatus.Completed,         // Saturday
            HistoricalStatus.OverCompleted,     // Sunday
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(7 + index), status)
        }

        completionHistory.slice(7..13).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(7 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val fullHistorySoFar = mutableListOf<CompletionHistoryEntry>()
        fullHistorySoFar.addAll(firstWeek)
        fullHistorySoFar.addAll(secondWeek)
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val lastTwoDays = listOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.NotCompleted,
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(14 + index), status)
        }
        fullHistorySoFar.addAll(lastTwoDays)

        completionHistory.slice(14..15).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(14 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = fullHistorySoFar.last().date)
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = -1, schedule = expectedResultingSchedule)

        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)
    }

    @Test
    fun `weekly schedule, due on explicit days of week, periodic separation disabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            periodSeparationEnabled = false,
            dueDaysOfWeek = listOf(
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            false,  // Thursday
            false,  // Friday       + backlog
            false,  // Saturday     + backlog
            true,   // Sunday       - backlog

            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            true,   // Thursday     - backlog
            false,  // Friday       + backlog
            true,   // Saturday
            true,   // Sunday       - backlog

            false,  // Monday
            false,  // Tuesday      + backlog
        )

        val firstSixDaysOfFirstWeek = listOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
            HistoricalStatus.Completed,
            HistoricalStatus.Skipped,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.NotCompleted,
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(index), status)
        }

        completionHistory.slice(0..5).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstSixDaysOfFirstWeek.first().date..firstSixDaysOfFirstWeek.last().date
            )
        ).isEqualTo(firstSixDaysOfFirstWeek)

        insertRoutineStatusIntoHistory(
            routineId = routineId,
            currentDate = routineStartDate.plusDays(6),
            completedOnCurrentDate = completionHistory[6],
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                routineStartDate.plusDays(6), HistoricalStatus.SortedOutBacklog
            )
        )
        firstWeek[5] = firstWeek[5].copy(status = HistoricalStatus.CompletedLater)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val secondWeek = listOf(
            HistoricalStatus.Skipped,           // Monday
            HistoricalStatus.Completed,         // Tuesday
            HistoricalStatus.Completed,         // Wednesday
            HistoricalStatus.SortedOutBacklog,  // Thursday
            HistoricalStatus.CompletedLater,    // Friday
            HistoricalStatus.Completed,         // Saturday
            HistoricalStatus.SortedOutBacklog,  // Sunday
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(7 + index), status)
        }

        completionHistory.slice(7..13).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(7 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val fullHistorySoFar = mutableListOf<CompletionHistoryEntry>()
        fullHistorySoFar.addAll(firstWeek)
        fullHistorySoFar.addAll(secondWeek)
        fullHistorySoFar[4] = fullHistorySoFar[4].copy(status = HistoricalStatus.CompletedLater)
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val lastTwoDays = listOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.NotCompleted,
        ).mapIndexed { index, status ->
            CompletionHistoryEntry(routineStartDate.plusDays(14 + index), status)
        }
        fullHistorySoFar.addAll(lastTwoDays)

        completionHistory.slice(14..15).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(14 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = fullHistorySoFar.last().date)
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = -1, schedule = expectedResultingSchedule)

        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)
    }

    @Test
    fun `periodic custom schedule, backlog enabled, completing ahead disabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.CustomDateSchedule(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = false,
            dueDates = listOf(
                routineStartDate.plusDays(1),
                routineStartDate.plusDays(2),
                routineStartDate.plusDays(7),
                routineStartDate.plusDays(12),
                routineStartDate.plusDays(14),
            )
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,
            true,
            false,  // + backlog
            false,
            false,
            true,   // - backlog
            false,
            true,
            false,
            true,   // + done ahead
            false,
            false,
            false,  // + backlog, because completing ahead disabled
            false,
            true,
        )

        completionHistory.slice(0..4).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val expectedEntriesList = mutableListOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.Skipped,
            HistoricalStatus.Skipped,
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..routineStartDate.plusDays(4)
            )
        ).isEqualTo(
            expectedEntriesList.mapIndexed { index, status ->
                CompletionHistoryEntry(routineStartDate.plusDays(index), status)
            }
        )

        expectedEntriesList.add(HistoricalStatus.SortedOutBacklog)
        expectedEntriesList[2] = HistoricalStatus.CompletedLater

        completionHistory.slice(5..14).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(5 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.Completed)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.OverCompleted)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.Completed)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(
            expectedEntriesList.mapIndexed { index, status ->
                CompletionHistoryEntry(routineStartDate.plusDays(index), status)
            }
        )

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = routineStartDate.plusDays(14))
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = -1, schedule = expectedResultingSchedule)

        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)
    }

    @Test
    fun `periodic custom schedule, backlog disabled, completing ahead enabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.CustomDateSchedule(
            routineStartDate = routineStartDate,
            backlogEnabled = false,
            cancelDuenessIfDoneAhead = true,
            dueDates = listOf(
                routineStartDate.plusDays(1),
                routineStartDate.plusDays(2),
                routineStartDate.plusDays(7),
                routineStartDate.plusDays(12),
                routineStartDate.plusDays(14),
            )
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,
            true,
            false,  // not completed but no backlog, because it's disabled
            false,
            false,
            true,   // + done ahead
            false,
            true,
            false,
            true,   // + done ahead
            false,
            false,
            false,  // - done ahead
            false,
            true,
        )

        completionHistory.slice(0..14).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val expectedEntriesList = listOf(
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.Skipped,
            HistoricalStatus.Skipped,
            HistoricalStatus.OverCompleted,
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
            HistoricalStatus.Skipped,
            HistoricalStatus.OverCompleted,
            HistoricalStatus.Skipped,
            HistoricalStatus.Skipped,
            HistoricalStatus.AlreadyCompleted,
            HistoricalStatus.Skipped,
            HistoricalStatus.Completed,
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(
            expectedEntriesList.mapIndexed { index, status ->
                CompletionHistoryEntry(routineStartDate.plusDays(index), status)
            }
        )

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = routineStartDate.plusDays(14))
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = 1, schedule = expectedResultingSchedule)

        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)
    }

    @Test
    fun `monthly schedule, not completed for a long time`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(1, 6, 9, 11, 26, 30),
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = listOf(
                WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Third),
                WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Forth),
                WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Fifth),
            ),
            startFromRoutineStart = false,
            periodSeparationEnabled = false,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            routineStartDate = routineStartDate,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 16),
            vacationEndDate = LocalDate(2023, Month.OCTOBER, 22),
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = mutableListOf<Boolean>()
        repeat(34) { completionHistory.add(false) }
        completionHistory.forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val expectedEntriesList = mutableListOf<HistoricalStatus>()
        repeat(4) { expectedEntriesList.add(HistoricalStatus.Skipped) }
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        repeat(2) { expectedEntriesList.add(HistoricalStatus.Skipped) }
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        repeat(4) { expectedEntriesList.add(HistoricalStatus.Skipped) }
        repeat(7) { expectedEntriesList.add(HistoricalStatus.OnVacation) }
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        expectedEntriesList.add(HistoricalStatus.Skipped)
        expectedEntriesList.add(HistoricalStatus.NotCompleted)
        repeat(3) { expectedEntriesList.add(HistoricalStatus.Skipped) }
        repeat(3) { expectedEntriesList.add(HistoricalStatus.NotCompleted) }
        repeat(3) { expectedEntriesList.add(HistoricalStatus.Skipped) }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(33)
            )
        ).isEqualTo(
            expectedEntriesList.mapIndexed { index, status ->
                CompletionHistoryEntry(routineStartDate.plusDays(index), status)
            }
        )

        val expectedResultingSchedule =
            schedule.copy(lastDateInHistory = routineStartDate.plusDays(33))
        val expectedResultingRoutine =
            routine.copy(scheduleDeviation = -8, schedule = expectedResultingSchedule)

        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(expectedResultingRoutine)
    }
}