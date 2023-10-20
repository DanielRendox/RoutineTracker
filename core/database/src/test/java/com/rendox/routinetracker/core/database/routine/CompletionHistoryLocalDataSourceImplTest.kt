package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import com.rendox.routinetracker.core.database.localDataSourceModule
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

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                localDataSourceModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getInsertCompletionHistoryEntry() = runTest {
        val completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )

        val history = mutableListOf<CompletionHistoryEntry>()
        val startDate = LocalDate(2023, Month.OCTOBER, 1)
        var dateCounter = startDate
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.FullyCompleted,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.OnVacation,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.NotCompleted,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)

        history.forEach { entry ->
            completionHistoryLocalDataSource.insertHistoryEntry(
                id = null,
                routineId = 1,
                entry = entry,
                tasksCompletedCounterIncrementAmount = null,
            )
        }

        val wholeHistory = completionHistoryLocalDataSource
            .getHistoryEntries(
                routineId = 1,
                dates = startDate..startDate.plusDays(history.lastIndex),
            )
        assertThat(wholeHistory).isEqualTo(history)

        val randomDateRange = generateRandomDateRange(
            minDate = startDate,
            maxDateInclusive = dateCounter,
        )
        val randomDateRangeIndices =
            startDate.daysUntil(randomDateRange.start)..startDate.daysUntil(randomDateRange.endInclusive)
        val randomPeriodInHistory = completionHistoryLocalDataSource
            .getHistoryEntries(
                routineId = 1,
                dates = randomDateRange,
            )
        val expectedPeriodInHistory = history.slice(randomDateRangeIndices)
        assertThat(randomPeriodInHistory).isEqualTo(expectedPeriodInHistory)

        val randomDate = startDate.plusDays(Random.nextInt(history.size))
        val singleDateRange = randomDate..randomDate
        assertThat(
            completionHistoryLocalDataSource.getHistoryEntries(
                routineId = 1,
                dates = singleDateRange,
            )
        ).isEqualTo(listOf(history[startDate.daysUntil(randomDate)]))
    }

    @Test
    fun insertEntriesAndUpdateStatusByDate() = runTest {
        val completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )

        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 1)

        val history = listOf(
            CompletionHistoryEntry(routineStartDate, HistoricalStatus.FullyCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(1), HistoricalStatus.Skipped),
            CompletionHistoryEntry(routineStartDate.plusDays(2), HistoricalStatus.PartiallyCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(3), HistoricalStatus.NotCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(4), HistoricalStatus.FullyCompleted),
        )

        for (entry in history) {
            completionHistoryLocalDataSource.insertHistoryEntry(
                routineId = routineId,
                entry = entry,
                tasksCompletedCounterIncrementAmount = null,
            )
        }

        val newEntry = CompletionHistoryEntry(
            date = routineStartDate.plusDays(2),
            status = HistoricalStatus.FullyCompleted,
        )

        completionHistoryLocalDataSource.updateHistoryEntryStatusByDate(
            routineId = routineId,
            date = newEntry.date,
            status = newEntry.status,
            tasksCompletedCounterIncrementAmount = null,
        )

        val updatedDateValue = completionHistoryLocalDataSource.getHistoryEntries(
            routineId = routineId,
            dates = newEntry.date..newEntry.date,
        )[0]

        assertThat(updatedDateValue).isEqualTo(newEntry)
    }

    @Test
    fun insertEntriesAndUpdateStatusByStatus() = runTest {
        val completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )

        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 1)

        val history = listOf(
            CompletionHistoryEntry(routineStartDate, HistoricalStatus.FullyCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(1), HistoricalStatus.Skipped),
            CompletionHistoryEntry(routineStartDate.plusDays(2), HistoricalStatus.PartiallyCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(3), HistoricalStatus.NotCompleted),
            CompletionHistoryEntry(routineStartDate.plusDays(4), HistoricalStatus.FullyCompleted),
        )

        for (entry in history) {
            completionHistoryLocalDataSource.insertHistoryEntry(
                routineId = routineId,
                entry = entry,
                tasksCompletedCounterIncrementAmount = null,
            )
        }

        val notCompletedUpdatedEntry = CompletionHistoryEntry(
            date = routineStartDate.plusDays(3),
            status = HistoricalStatus.FullyCompleted,
        )

        val partiallyCompletedUpdatedEntry = CompletionHistoryEntry(
            date = routineStartDate.plusDays(2),
            status = HistoricalStatus.FullyCompleted,
        )

        val backlogStatuses = listOf(
            HistoricalStatus.PartiallyCompleted,
            HistoricalStatus.NotCompleted,
        )

        completionHistoryLocalDataSource.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            tasksCompletedCounterIncrementAmount = null,
            newStatus = notCompletedUpdatedEntry.status,
            matchingStatuses = backlogStatuses,
        )

        val notCompletedUpdatedEntryDbValue = completionHistoryLocalDataSource.getHistoryEntries(
            routineId = routineId,
            dates = notCompletedUpdatedEntry.date..notCompletedUpdatedEntry.date,
        )[0]
        assertThat(notCompletedUpdatedEntryDbValue).isEqualTo(notCompletedUpdatedEntry)

        completionHistoryLocalDataSource.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            tasksCompletedCounterIncrementAmount = null,
            newStatus = partiallyCompletedUpdatedEntry.status,
            matchingStatuses = backlogStatuses,
        )

        val partiallyCompletedUpdatedEntryDbValue = completionHistoryLocalDataSource.getHistoryEntries(
            routineId = routineId,
            dates = partiallyCompletedUpdatedEntry.date..partiallyCompletedUpdatedEntry.date,
        )[0]
        assertThat(partiallyCompletedUpdatedEntryDbValue).isEqualTo(partiallyCompletedUpdatedEntry)
    }
}