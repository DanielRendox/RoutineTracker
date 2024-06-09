package com.rendox.routinetracker.core.database.vacation

import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface VacationLocalDataSource {
    suspend fun getVacationByDate(
        habitId: Long, date: LocalDate
    ): Vacation?

    suspend fun getPreviousVacation(
        habitId: Long,
        currentDate: LocalDate,
    ): Vacation?

    suspend fun getLastVacation(habitId: Long): Vacation?

    suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): List<Vacation>

    suspend fun getAllVacations(): List<Pair<Long, Vacation>>
    suspend fun insertVacation(habitId: Long, vacation: Vacation)
    suspend fun deleteVacationById(id: Long)
}