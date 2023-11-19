package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

//class RoutineLocalDataSourceFake(
//    private val routineData: RoutineData
//) : RoutineLocalDataSource {
//
//    override suspend fun getRoutineById(routineId: Long): Routine {
//        val routine = routineData.listOfRoutines[(routineId - 1).toInt()]
//        val lastDateInHistory: LocalDate? = if (routineData.completionHistory.isEmpty()) {
//            null
//        } else {
//            routineData.completionHistory.last().second.date
//        }
//        return routine.createNewRoutineWithUpdatedProperties(lastDateInHistory)
//    }
//
//    override suspend fun insertRoutine(routine: Routine) {
//        routineData.listOfRoutines =
//            routineData.listOfRoutines.toMutableList().apply { add(routine) }
//    }
//
//    override suspend fun updateScheduleDeviation(newValue: Int, routineId: Long) {
//        val newRoutine =
//            when (val oldRoutine = routineData.listOfRoutines[(routineId - 1).toInt()]) {
//                is Routine.YesNoRoutine -> oldRoutine.copy(
//                    scheduleDeviation = newValue
//                )
//            }
//        routineData.listOfRoutines =
//            routineData.listOfRoutines.toMutableList()
//                .apply { set((routineId - 1).toInt(), newRoutine) }
//    }
//
//    private fun Routine.createNewRoutineWithUpdatedProperties(
//        lastDateInHistory: LocalDate?
//    ): Routine {
//        var schedule = schedule
//        schedule = when (schedule) {
//            is Schedule.EveryDaySchedule -> schedule.copy(lastDateInHistory = lastDateInHistory)
//            is Schedule.WeeklyScheduleByDueDaysOfWeek ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.WeeklyScheduleByNumOfDueDays ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.MonthlyScheduleByDueDatesIndices ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.MonthlyScheduleByNumOfDueDays ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.PeriodicCustomSchedule ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.CustomDateSchedule ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.AnnualScheduleByDueDates ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//
//            is Schedule.AnnualScheduleByNumOfDueDays ->
//                schedule.copy(lastDateInHistory = lastDateInHistory)
//        }
//        return when (this) {
//            is Routine.YesNoRoutine -> copy(schedule = schedule)
//        }
//    }
//
//    override fun getAllRoutines(): Flow<List<Routine>> {
//        TODO()
//    }
//}