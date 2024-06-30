package com.rendox.routinetracker.core.database.vacation

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface VacationLocalDataSource {

    suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Vacation>

    suspend fun getMultiHabitVacations(
        habitsToPeriods: List<Pair<List<Long>, LocalDateRange>>,
    ): Map<Long, List<Vacation>>

    suspend fun insertVacation(
        habitId: Long,
        vacation: Vacation,
    )
    suspend fun insertVacations(habitIdsToVacations: Map<Long, List<Vacation>>)
    suspend fun deleteVacationById(id: Long)
}