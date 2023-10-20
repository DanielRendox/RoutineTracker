package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine

class RoutineLocalDataSourceFake : RoutineLocalDataSource {

    private val lock = Any()
    private var listOfRoutines = emptyList<Routine>()
        get() {
            synchronized(lock) {
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }

    override suspend fun getRoutineById(routineId: Long): Routine {
        return listOfRoutines[(routineId - 1).toInt()]
    }

    override suspend fun insertRoutine(routine: Routine) {
        listOfRoutines = listOfRoutines.toMutableList().apply { add(routine) }
    }
}