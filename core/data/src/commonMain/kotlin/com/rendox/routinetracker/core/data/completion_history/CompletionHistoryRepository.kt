package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface CompletionHistoryRepository {
    suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double

    suspend fun getRecordByDate(habitId: Long, date: LocalDate): Habit.CompletionRecord?
    suspend fun getLastCompletedRecord(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): Habit.CompletionRecord?

    suspend fun getFirstCompletedRecord(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): Habit.CompletionRecord?

    /**
     * get all records for the given habit in the ascending order, if [minDate] or [maxDate] are
     * specified, returns only the records in the given range.
     */
    suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): List<Habit.CompletionRecord>

    suspend fun getAllRecords(): List<Pair<Long, Habit.CompletionRecord>>

    suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    )

    suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    )
}