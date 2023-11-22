package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepositoryImpl
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepositoryImpl
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
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

class InsertRoutineStatusUseCaseTest {

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
    private val insertRoutineStatusIntoHistory = InsertRoutineStatusUseCase(
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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 1),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompletedOnVacation,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
        )

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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )

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
            currentDate = LocalDate(2023, Month.OCTOBER, 8),
            completedOnCurrentDate = completionHistory[6],
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        firstWeek[5] = firstWeek[5].copy(
            status = HistoricalStatus.CompletedLater,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val secondWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )

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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 17),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )
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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )

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
            currentDate = LocalDate(2023, Month.OCTOBER, 8),
            completedOnCurrentDate = completionHistory[6],
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        firstWeek[5] = firstWeek[5].copy(status = HistoricalStatus.CompletedLater)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val secondWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )

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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 17),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )
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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..routineStartDate.plusDays(4)
            )
        ).isEqualTo(expectedEntriesList)

        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )
        expectedEntriesList[2] = CompletionHistoryEntry(
            date = LocalDate(2023, Month.OCTOBER, 4),
            status = HistoricalStatus.CompletedLater,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )

        completionHistory.slice(5..14).forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(5 + index),
                completedOnCurrentDate = isCompleted,
            )
        }

        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(expectedEntriesList)
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
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(expectedEntriesList)
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
        repeat(17) { completionHistory.add(false) }
        repeat(2) { completionHistory.add(true) }
        repeat(15) { completionHistory.add(false) }
        completionHistory.forEachIndexed { index, isCompleted ->
            insertRoutineStatusIntoHistory(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
            )
        }

        val expectedEntriesList = mutableListOf<CompletionHistoryEntry>()
        repeat(4) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 2),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 7),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(4) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 12),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 16),
                    status = HistoricalStatus.NotCompletedOnVacation,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 19),
                    status = HistoricalStatus.SortedOutBacklogOnVacation,
                    scheduleDeviation = 1F,
                    timesCompleted = 1F,
                )
            )
        }
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 21),
                    status = HistoricalStatus.NotCompletedOnVacation,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 23),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 24),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 25),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 26),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 27),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = routineStartDate.plusDays(it + 28),
                    status = HistoricalStatus.NotCompleted,
                    scheduleDeviation = -1F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = routineStartDate.plusDays(it + 31),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(33)
            )
        ).isEqualTo(expectedEntriesList)
    }
}