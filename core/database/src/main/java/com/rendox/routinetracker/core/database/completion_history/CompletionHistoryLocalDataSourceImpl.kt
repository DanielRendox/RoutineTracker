package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.database.CompletionHistoryEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : CompletionHistoryLocalDataSource {

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange,
    ): List<CompletionHistoryEntry> {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getHistoryEntriesByIndices(
                routineId = routineId,
                start = dates.start,
                end = dates.endInclusive,
            ).executeAsList().map { it.toExternalModel() }
        }
    }

    private fun CompletionHistoryEntity.toExternalModel() =
        CompletionHistoryEntry(date = date, status = status)

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
        scheduleDeviationIncrementAmount: Int,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                db.completionHistoryEntityQueries.insertHistoryEntry(
                    id = id,
                    routineId = routineId,
                    date = entry.date,
                    status = entry.status,
                )
                db.routineEntityQueries.incrementScheduleDeviation(
                    incrementAmount = scheduleDeviationIncrementAmount,
                    routineId = routineId,
                )
            }
        }
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                db.completionHistoryEntityQueries.updateHistoryEntryStatusByDate(
                    status = status,
                    routineId = routineId,
                    date = date
                )
                db.routineEntityQueries.incrementScheduleDeviation(
                    incrementAmount = scheduleDeviationIncrementAmount,
                    routineId = routineId,
                )
            }
        }
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
        matchingStatuses: List<HistoricalStatus>,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                db.completionHistoryEntityQueries.updateLastHistoryEntryStatusByStatus(
                    newStatus = newStatus,
                    routineId = routineId,
                    statusPredicate = matchingStatuses,
                )
                db.routineEntityQueries.incrementScheduleDeviation(
                    incrementAmount = scheduleDeviationIncrementAmount,
                    routineId = routineId,
                )
            }
        }
    }

    override suspend fun getFirstHistoryEntryDate(routineId: Long): LocalDate? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getFirstHistoryEntryDate(routineId)
                .executeAsOneOrNull()
        }
    }

    override suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getLastHistoryEntryDate(routineId)
                .executeAsOneOrNull()
        }
    }

    override suspend fun getFirstHistoryEntryDateByStatus(
        routineId: Long,
        startingFromDate: LocalDate,
        matchingStatuses: List<HistoricalStatus>,
    ): LocalDate? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getFirstHistoryEntryDateByStatus(
                routineId, startingFromDate, matchingStatuses
            ).executeAsOneOrNull()
        }
    }
}