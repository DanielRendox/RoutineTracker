package com.rendox.routinetracker.core.database.vacation

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.toExternalModel
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.epochDate
import com.rendox.routinetracker.core.model.Vacation
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class VacationLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : VacationLocalDataSource {

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Vacation> = withContext(ioDispatcher) {
        db.vacationEntityQueries.getVacationsInPeriod(
            habitId = habitId,
            minDate = minDate,
            maxDate = maxDate,
        ).executeAsList().map { it.toExternalModel() }
    }

    override suspend fun getMultiHabitVacations(
        habitsToPeriods: List<Pair<List<Long>, LocalDateRange>>,
    ): Map<Long, List<Vacation>> = withContext(ioDispatcher) {
        require(habitsToPeriods.size <= 5) {
            "Only up to 5 habit types can be queried at once"
        }

        db.vacationEntityQueries
            // this is a nasty workaround because SQLDelight does not support dynamic queries
            .getMultiHabitVacations(
                habitIds1 = habitsToPeriods.getOrNull(0)?.first ?: emptyList(),
                habitIds2 = habitsToPeriods.getOrNull(1)?.first ?: emptyList(),
                habitIds3 = habitsToPeriods.getOrNull(2)?.first ?: emptyList(),
                habitIds4 = habitsToPeriods.getOrNull(3)?.first ?: emptyList(),
                habitIds5 = habitsToPeriods.getOrNull(4)?.first ?: emptyList(),
                minDate1 = habitsToPeriods.getOrNull(0)?.second?.start ?: epochDate,
                minDate2 = habitsToPeriods.getOrNull(1)?.second?.start ?: epochDate,
                minDate3 = habitsToPeriods.getOrNull(2)?.second?.start ?: epochDate,
                minDate4 = habitsToPeriods.getOrNull(3)?.second?.start ?: epochDate,
                minDate5 = habitsToPeriods.getOrNull(4)?.second?.start ?: epochDate,
                maxDate1 = habitsToPeriods.getOrNull(0)?.second?.endInclusive ?: epochDate,
                maxDate2 = habitsToPeriods.getOrNull(1)?.second?.endInclusive ?: epochDate,
                maxDate3 = habitsToPeriods.getOrNull(2)?.second?.endInclusive ?: epochDate,
                maxDate4 = habitsToPeriods.getOrNull(3)?.second?.endInclusive ?: epochDate,
                maxDate5 = habitsToPeriods.getOrNull(4)?.second?.endInclusive ?: epochDate,
            ).executeAsList().groupBy(
                keySelector = { it.habitId },
                valueTransform = { it.toExternalModel() },
            )
    }

    override suspend fun insertVacation(
        habitId: Long,
        vacation: Vacation,
    ) = withContext(ioDispatcher) {
        db.vacationEntityQueries.insertVacation(
            habitId = habitId,
            id = vacation.id,
            startDate = vacation.startDate,
            endDate = vacation.endDate,
        )
    }

    override suspend fun insertVacations(habitIdsToVacations: Map<Long, List<Vacation>>) = withContext(ioDispatcher) {
        db.vacationEntityQueries.transaction {
            for ((habitId, vacations) in habitIdsToVacations) {
                for (vacation in vacations) {
                    db.vacationEntityQueries.insertVacation(
                        habitId = habitId,
                        id = vacation.id,
                        startDate = vacation.startDate,
                        endDate = vacation.endDate,
                    )
                }
            }
        }
    }

    override suspend fun deleteVacationById(id: Long) = withContext(ioDispatcher) {
        db.vacationEntityQueries.deleteVacationById(id)
    }
}