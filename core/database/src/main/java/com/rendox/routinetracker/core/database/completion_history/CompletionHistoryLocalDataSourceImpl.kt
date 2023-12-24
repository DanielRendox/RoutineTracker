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

    override suspend fun getHistoryEntryByDate(
        routineId: Long,
        date: LocalDate
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getHistoryEntryByDate(
                routineId = routineId,
                date = date,
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    private fun CompletionHistoryEntity.toExternalModel() = CompletionHistoryEntry(
        date = date,
        status = status,
        scheduleDeviation = scheduleDeviation,
        timesCompleted = timesCompleted,
    )

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
    ) {
        withContext(dispatcher) {
            db.habitEntityQueries.transaction {
                db.completionHistoryEntityQueries.insertHistoryEntry(
                    id = id,
                    routineId = routineId,
                    date = entry.date,
                    status = entry.status,
                    scheduleDeviation = entry.scheduleDeviation,
                    timesCompleted = entry.timesCompleted,
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

    override suspend fun updateHistoryEntryByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus?,
        newScheduleDeviation: Float?,
        newTimesCompleted: Float?,
    ) {
        withContext(dispatcher) {
            db.habitEntityQueries.transaction {
                val oldValue = db.completionHistoryEntityQueries.getHistoryEntriesByIndices(
                    routineId, date, date
                ).executeAsOne()
                db.completionHistoryEntityQueries.updateHistoryEntryStatusByDate(
                    status = newStatus ?: oldValue.status,
                    routineId = routineId,
                    date = date,
                    scheduleDeviation = newScheduleDeviation ?: oldValue.scheduleDeviation,
                    timesCompleted = newTimesCompleted ?: oldValue.timesCompleted,
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
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getFirstHistoryEntryByStatus(
                routineId = routineId,
                matchingStatuses = matchingStatuses,
                minDate = minDate,
                maxDate = maxDate,
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries.getLastHistoryEntryByStatus(
                routineId = routineId,
                matchingStatuses = matchingStatuses,
                minDate = minDate,
                maxDate = maxDate,
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

    override suspend fun getTotalTimesCompletedInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries
                .getTotalTimesCompletedAtTheMomentOfDate(routineId, startDate, endDate)
                .executeAsOne()
        }
    }

    override suspend fun getScheduleDeviationInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double {
        return withContext(dispatcher) {
            db.completionHistoryEntityQueries
                .getScheduleDeviationAtTheMomentOfDate(routineId, startDate, endDate)
                .executeAsOne()
        }
    }
}