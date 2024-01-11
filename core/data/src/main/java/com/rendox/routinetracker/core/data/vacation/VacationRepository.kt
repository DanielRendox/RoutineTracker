package com.rendox.routinetracker.core.data.vacation

import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface VacationRepository {

    /** Returns a [Vacation] that includes [date]. */
    suspend fun getVacationByDate(
        habitId: Long, date: LocalDate
    ): Vacation?

    /** Returns a [Vacation] that was ended before the [currentDate]. */
    suspend fun getPreviousVacation(
        habitId: Long,
        currentDate: LocalDate,
    ): Vacation?

    suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): List<Vacation>

    suspend fun getLastVacation(habitId: Long): Vacation?
    suspend fun insertVacation(habitId: Long, vacation: Vacation)
    suspend fun deleteVacationById(id: Long)
}