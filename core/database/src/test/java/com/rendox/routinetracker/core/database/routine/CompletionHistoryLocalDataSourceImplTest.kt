package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.logic.time.epochDate
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

        for (status in HistoricalStatus.values().toList()) {
            val scheduleDeviation = when (status) {
                HistoricalStatus.NotCompleted -> -1F
                HistoricalStatus.Completed -> 0F
                HistoricalStatus.OverCompleted -> 1F
                HistoricalStatus.OverCompletedOnVacation -> 1F
                HistoricalStatus.SortedOutBacklogOnVacation -> 1F
                HistoricalStatus.Skipped -> 0F
                HistoricalStatus.NotCompletedOnVacation -> 0F
                HistoricalStatus.CompletedLater -> 0F
                HistoricalStatus.SortedOutBacklog -> 1F
                HistoricalStatus.AlreadyCompleted -> 0F
            }
            val timesCompleted = when (status) {
                HistoricalStatus.Completed,
                HistoricalStatus.OverCompleted,
                HistoricalStatus.OverCompletedOnVacation,
                HistoricalStatus.SortedOutBacklog,
                HistoricalStatus.SortedOutBacklogOnVacation -> 1F
                else -> 0F
            }
            repeat(50) {
                dateCounter = dateCounter.plusDays(1)
                history.add(
                    CompletionHistoryEntry(
                        date = dateCounter,
                        status = status,
                        scheduleDeviation = scheduleDeviation,
                        timesCompleted = timesCompleted,
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
            maxDate = endDate,
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

        completionHistoryLocalDataSource.updateHistoryEntryByDate(
            routineId = routineId,
            date = notCompletedLaterEntry.date,
            newStatus = HistoricalStatus.CompletedLater,
            newScheduleDeviation = notCompletedLaterEntry.scheduleDeviation,
            newTimesCompleted = notCompletedLaterEntry.timesCompleted,
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
        completionHistoryLocalDataSource.updateHistoryEntryByDate(
            routineId = routineId,
            date = completedLaterStatus.date,
            newStatus = HistoricalStatus.NotCompleted,
            newScheduleDeviation = -1F,
            newTimesCompleted = 0F,
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

    @Test
    fun assertTotalTimesCompletedInPeriodReturnsCorrectValue() = runTest {
        val datePeriod = generateRandomDateRange(
            minDate = completionHistory.first().date,
            maxDate = completionHistory.last().date,
        )
        val expectedValue = completionHistory
            .filter { it.date >= datePeriod.start && it.date <= datePeriod.endInclusive }
            .map { it.timesCompleted }
            .sum().toDouble()
        assertThat(
            completionHistoryLocalDataSource.getTotalTimesCompletedInPeriod(
                routineId = routineId,
                startDate = datePeriod.start,
                endDate = datePeriod.endInclusive,
            )
        ).isEqualTo(expectedValue)
    }

    @Test
    fun assertTotalTimesCompletedInPeriodReturnsZeroIfPeriodIsNotPresent() = runTest {
        val datePeriod = epochDate..epochDate
        assertThat(
            completionHistoryLocalDataSource.getScheduleDeviationInPeriod(
                routineId = routineId,
                startDate = datePeriod.start,
                endDate = datePeriod.endInclusive,
            )
        ).isZero()
    }

    @Test
    fun assertScheduleDeviationInPeriodReturnsCorrectValue() = runTest {
        val datePeriod = generateRandomDateRange(
            minDate = completionHistory.first().date,
            maxDate = completionHistory.last().date,
        )
        val expectedValue = completionHistory
            .filter { it.date >= datePeriod.start && it.date <= datePeriod.endInclusive }
            .map { it.scheduleDeviation }
            .sum().toDouble()
        assertThat(
            completionHistoryLocalDataSource.getScheduleDeviationInPeriod(
                routineId = routineId,
                startDate = datePeriod.start,
                endDate = datePeriod.endInclusive,
            )
        ).isEqualTo(expectedValue)
    }

    @Test
    fun assertScheduleDeviationInPeriodReturnsZeroIfPeriodIsNotPresent() = runTest {
        val datePeriod = epochDate..epochDate
        assertThat(
            completionHistoryLocalDataSource.getScheduleDeviationInPeriod(
                routineId = routineId,
                startDate = datePeriod.start,
                endDate = datePeriod.endInclusive,
            )
        ).isZero()
    }
}