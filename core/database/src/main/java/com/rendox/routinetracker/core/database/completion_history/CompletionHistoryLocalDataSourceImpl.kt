package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.completionhistory.CompletedLaterHistoryEntity
import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryEntity
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

    private fun CompletionHistoryEntity.toExternalModel() = CompletionHistoryEntry(
        date = date,
        status = status,
        currentScheduleDeviation = currentScheduleDeviation
    )

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                db.completionHistoryEntityQueries.insertHistoryEntry(
                    id = id,
                    routineId = routineId,
                    date = entry.date,
                    status = entry.status,
                    currentScheduleDeviation = entry.currentScheduleDeviation,
                )
                if (entry.status == HistoricalStatus.CompletedLater) {
                    insertCompletedLaterDate(routineId, entry.date)
                }
            }
        }
    }

    override suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate) {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.deleteHistoryEntry(routineId, date)
        }
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                db.completionHistoryEntityQueries.updateHistoryEntryStatusByDate(
                    status = newStatus,
                    routineId = routineId,
                    date = date,
                    currentScheduleDeviation = newScheduleDeviation,
                )
                if (newStatus == HistoricalStatus.CompletedLater) {
                    insertCompletedLaterDate(routineId, date)
                }
            }
        }
    }

    override suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getFirstHistoryEntry(routineId)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getLastHistoryEntry(routineId)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getFirstHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getFirstHistoryEntryByStatus(
                routineId, matchingStatuses
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastHistoryEntryByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getLastHistoryEntryByStatus(
                routineId, matchingStatuses
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean {
        return withContext(dispatcher) {
            checkCompletedLater(routineId, date)
        }
    }

    override suspend fun deleteCompletedLaterBackupEntry(
        routineId: Long, date: LocalDate
    ) {
        withContext(dispatcher) {
            db.completedLaterHistoryEntityQueries.deleteCompletedLaterDate(
                routineId, date
            )
        }
    }

    private fun checkCompletedLater(routineId: Long, date: LocalDate): Boolean {
        val completedLaterEntity: CompletedLaterHistoryEntity? =
            db.completedLaterHistoryEntityQueries.getEntityByDate(
                routineId, date
            ).executeAsOneOrNull()
        return completedLaterEntity != null
    }

    private fun insertCompletedLaterDate(routineId: Long, date: LocalDate) {
        val alreadyInserted = checkCompletedLater(routineId, date)
        if (!alreadyInserted) {
            db.completedLaterHistoryEntityQueries.insertCompletedLaterDate(
                id = null,
                routineId = routineId,
                date = date,
            )
        }
    }
}