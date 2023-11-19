package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.logic.time.generateRandomDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.random.Random

class CompletionHistoryLocalDataSourceImplTest : KoinTest {
    private lateinit var sqlDriver: SqlDriver
    private lateinit var completionHistoryLocalDataSource: CompletionHistoryLocalDataSourceImpl
    private lateinit var completionHistory: List<CompletionHistoryEntry>
    private val routineId = 1L
    private val startDate = LocalDate(2023, Month.OCTOBER, 1)
    private lateinit var endDate: LocalDate

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    @Before
    fun setUp() = runTest {
        startKoin {
            modules(
                localDataSourceModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)

        completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )

        val history = mutableListOf<CompletionHistoryEntry>()
        var dateCounter = startDate
        var scheduleDeviation = 0

        for (status in HistoricalStatus.values().toList()) {
            val scheduleDeviationIncrementAmount = when (status) {
                HistoricalStatus.NotCompleted -> -1
                HistoricalStatus.Completed -> 0
                HistoricalStatus.OverCompleted -> 1
                HistoricalStatus.OverCompletedOnVacation -> 1
                HistoricalStatus.SortedOutBacklogOnVacation -> 1
                HistoricalStatus.Skipped -> 0
                HistoricalStatus.NotCompletedOnVacation -> 0
                HistoricalStatus.CompletedLater -> 0
                HistoricalStatus.SortedOutBacklog -> 1
                HistoricalStatus.AlreadyCompleted -> 0
            }
            repeat(50) {
                scheduleDeviation += scheduleDeviationIncrementAmount
                dateCounter = dateCounter.plusDays(1)
                history.add(
                    CompletionHistoryEntry(
                        date = dateCounter,
                        status = status,
                        currentScheduleDeviation = scheduleDeviation,
                    )
                )
            }
        }

        history.shuffle()
        history.forEachIndexed { index, entry ->
            history[index] = entry.copy(date = startDate.plusDays(index))
        }

        completionHistory = history
        endDate = dateCounter

        for (historyEntry in history) {
            completionHistoryLocalDataSource.insertHistoryEntry(
                routineId = routineId,
                entry = historyEntry,
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun assertAllHistoryIsEqualToExpected() = runTest {
        val wholeHistory = completionHistoryLocalDataSource.getHistoryEntries(
            routineId = routineId,
            dates = startDate..startDate.plusDays(completionHistory.lastIndex),
        )
        assertThat(wholeHistory).isEqualTo(completionHistory)
    }

    @Test
    fun assertReturnsCorrectEntriesForDateRange() = runTest {
        val randomDateRange = generateRandomDateRange(
            minDate = startDate,
            maxDateInclusive = endDate,
        )
        val randomDateRangeIndices =
            startDate.daysUntil(randomDateRange.start)..startDate.daysUntil(randomDateRange.endInclusive)
        val randomPeriodInHistory = completionHistoryLocalDataSource.getHistoryEntries(
            routineId = routineId,
            dates = randomDateRange,
        )
        val expectedPeriodInHistory = completionHistory.slice(randomDateRangeIndices)
        assertThat(randomPeriodInHistory).isEqualTo(expectedPeriodInHistory)
    }

    @Test
    fun assertReturnsCorrectEntryForSingleDate() = runTest {
        val randomDate = startDate.plusDays(Random.nextInt(completionHistory.size))
        val singleDateRange = randomDate..randomDate
        assertThat(
            completionHistoryLocalDataSource.getHistoryEntries(
                routineId = routineId,
                dates = singleDateRange,
            )
        ).isEqualTo(listOf(completionHistory[startDate.daysUntil(randomDate)]))
    }

    @Test
    fun assertReturnsCorrectFirstHistoryEntry() = runTest {
        assertThat(
            completionHistoryLocalDataSource.getFirstHistoryEntry(routineId)
        ).isEqualTo(completionHistory.firstOrNull())
    }

    @Test
    fun assertReturnsCorrectLastHistoryEntry() = runTest {
        assertThat(
            completionHistoryLocalDataSource.getLastHistoryEntry(routineId)
        ).isEqualTo(completionHistory.lastOrNull())
    }

    @Test
    fun assertReturnsCorrectLastHistoryEntryDateByStatus() = runTest {
        val desiredStatuses = listOf(HistoricalStatus.NotCompleted, HistoricalStatus.Skipped)
        assertThat(
            completionHistoryLocalDataSource.getLastHistoryEntryByStatus(
                routineId = routineId,
                matchingStatuses = desiredStatuses,
            )
        ).isEqualTo(completionHistory.findLast { it.status in desiredStatuses })
    }

    @Test
    fun assertReturnsCorrectFirstHistoryEntryDateByStatus() = runTest {
        val desiredStatuses = listOf(HistoricalStatus.Completed, HistoricalStatus.OverCompleted)
        assertThat(
            completionHistoryLocalDataSource.getFirstHistoryEntryByStatus(
                routineId = routineId,
                matchingStatuses = desiredStatuses,
            )
        ).isEqualTo(completionHistory.find { it.status in desiredStatuses })
    }

    @Test
    fun assertInsertsCompletedLaterStatusesIntoSeparateTableOnInsert() = runTest {
        val completedLaterEntries = completionHistory.filter {
            it.status == HistoricalStatus.CompletedLater
        }

        for (entry in completedLaterEntries) {
            assertThat(
                completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                    routineId = routineId,
                    date = entry.date,
                )
            ).isTrue()
        }
    }

    @Test
    fun assertInsertsCompletedLaterStatusesIntoSeparateTableOnUpdate() = runTest {
        val notCompletedLaterEntry = completionHistory.find {
            it.status != HistoricalStatus.CompletedLater
        }!!

        assertThat(
            completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                routineId = routineId,
                date = notCompletedLaterEntry.date,
            )
        ).isFalse()

        completionHistoryLocalDataSource.updateHistoryEntryStatusByDate(
            routineId = routineId,
            date = notCompletedLaterEntry.date,
            newStatus = HistoricalStatus.CompletedLater,
            newScheduleDeviation = notCompletedLaterEntry.currentScheduleDeviation,
        )


        assertThat(
            completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                routineId = routineId,
                date = notCompletedLaterEntry.date,
            )
        ).isTrue()
    }

    @Test
    fun assertDoesNotRecordOtherEntriesExceptCompletedLaterIntoSeparateTable() = runTest {
        val notCompletedLaterEntries = completionHistory.filter {
            it.status != HistoricalStatus.CompletedLater
        }

        for (entry in notCompletedLaterEntries) {
            assertThat(
                completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                    routineId = routineId,
                    date = entry.date,
                )
            ).isFalse()
        }
    }

    @Test
    fun assertDeletesHistoryEntries() = runTest {
        val randomEntryIndex = Random.nextInt(completionHistory.size)
        val randomEntry = completionHistory[randomEntryIndex]
        completionHistoryLocalDataSource.deleteHistoryEntry(
            routineId = routineId,
            date = randomEntry.date,
        )
        val newHistory = completionHistory.toMutableList().apply { removeAt(randomEntryIndex) }
        assertThat(
            completionHistoryLocalDataSource.getHistoryEntries(
                routineId = routineId,
                dates = startDate..startDate.plusDays(completionHistory.lastIndex),
            )
        ).isEqualTo(newHistory)
    }

    @Test
    fun assertDoesNotDeleteCompletedLaterBackupEntriesOnStatusUpdate() = runTest {
        val completedLaterStatus = completionHistory.find {
            it.status == HistoricalStatus.CompletedLater
        }!!
        completionHistoryLocalDataSource.updateHistoryEntryStatusByDate(
            routineId = routineId,
            date = completedLaterStatus.date,
            newStatus = HistoricalStatus.NotCompleted,
            newScheduleDeviation = completedLaterStatus.currentScheduleDeviation - 1,
        )
        assertThat(
            completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                routineId = routineId,
                date = completedLaterStatus.date,
            )
        ).isTrue()
    }

    @Test
    fun assertDoesNotDeleteCompletedLaterBackupEntriesOnStatusDelete() = runTest {
        val completedLaterStatus = completionHistory.find {
            it.status == HistoricalStatus.CompletedLater
        }!!
        completionHistoryLocalDataSource.deleteHistoryEntry(
            routineId = routineId,
            date = completedLaterStatus.date,
        )
        assertThat(
            completionHistoryLocalDataSource.checkIfStatusWasCompletedLater(
                routineId = routineId,
                date = completedLaterStatus.date,
            )
        ).isTrue()
    }
}