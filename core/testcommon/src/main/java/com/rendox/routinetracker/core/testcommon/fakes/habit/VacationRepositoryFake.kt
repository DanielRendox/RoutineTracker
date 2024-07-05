package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class VacationRepositoryFake(
    private val habitData: HabitData,
) : VacationRepository {

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Vacation> = habitData.vacationHistory.value.filter {
        val vacationStartDate = it.second.startDate
        val vacationEndDate = it.second.endDate
        it.first == habitId &&
            (vacationEndDate == null || minDate <= vacationEndDate) &&
            maxDate >= vacationStartDate
    }.map { it.second }

    override suspend fun insertVacation(
        habitId: Long,
        vacation: Vacation,
    ) {
        habitData.vacationHistory.update {
            it.toMutableList().apply { add(habitId to vacation) }
        }
    }

    override suspend fun getMultiHabitVacations(
        habitsToPeriods: List<Pair<List<Long>, LocalDateRange>>,
    ): Map<Long, List<Vacation>> = getAllVacations()

    override suspend fun insertVacations(habitIdsToVacations: Map<Long, List<Vacation>>) {
        habitData.vacationHistory.update {
            habitIdsToVacations.flatMap { entry ->
                entry.value.map { entry.key to it }
            }
        }
    }

    private fun getAllVacations(): Map<Long, List<Vacation>> = habitData.vacationHistory.value.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second },
    )
}