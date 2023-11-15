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
                    db.completedLaterHistoryEntityQueries.insertCompletedLaterDate(
                        id = null,
                        routineId = routineId,
                        date = entry.date,
                    )
                }
            }
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
                    db.completedLaterHistoryEntityQueries.insertCompletedLaterDate(
                        id = null,
                        routineId = routineId,
                        date = date,
                    )
                }
            }
        }
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
        matchingStatuses: List<HistoricalStatus>,
    ) {
        withContext(dispatcher) {
            db.routineEntityQueries.transaction {
                val updatedDate =
                    db.completionHistoryEntityQueries.findLastHistoryEntryByStatus(
                        routineId = routineId,
                        statusPredicate = matchingStatuses,
                    ).executeAsOne()
                db.completionHistoryEntityQueries.updateLastHistoryEntryStatusByStatus(
                    newStatus = newStatus,
                    routineId = routineId,
                    statusPredicate = matchingStatuses,
                    currentScheduleDeviation = newScheduleDeviation,
                )
                if (updatedDate.status == HistoricalStatus.CompletedLater) {
                    db.completedLaterHistoryEntityQueries.insertCompletedLaterDate(
                        id = null,
                        routineId = routineId,
                        date = updatedDate.date,
                    )
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

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean {
        return withContext(dispatcher) {
            db.completedLaterHistoryEntityQueries.getEntityByDate(
                routineId, date
            ).executeAsOneOrNull() != null
        }
    }

    override suspend fun insertCompletedLaterDate(id: Long?, routineId: Long, date: LocalDate) {
        withContext(dispatcher) {
            db.completedLaterHistoryEntityQueries.insertCompletedLaterDate(id, routineId, date)
        }
    }

    override suspend fun deleteCompletedLaterDate(routineId: Long, date: LocalDate) {
        withContext(dispatcher) {
            db.completedLaterHistoryEntityQueries.deleteCompletedLaterDate(routineId, date)
        }
    }

    override suspend fun findLastHistoryEntryDateByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.findLastHistoryEntryByStatus(
                routineId, matchingStatuses
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate) {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.deleteHistoryEntry(routineId, date)
        }
    }
}