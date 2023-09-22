package com.rendox.routinetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.routine.data.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineViewModel(
    private val routineRepository: RoutineRepository,
    private val routineId: Long,
) : ViewModel() {

    private val _routineScreenState = MutableStateFlow(RoutineScreenState())
    val routineScreenState = _routineScreenState.asStateFlow()

    init {
        refreshRoutine()
    }

    private fun refreshRoutine() {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineById(routineId)
            println("Routine in fact: $routine")
            routine?.let {
                println("RoutineViewModel: updating routineScreenState")
                _routineScreenState.update { stateValue ->
                    stateValue.copy(
                        routineName = it.name,
                        routineStartDate = it.startDate.toString(),
                    )
                }
            }
        }
    }
}

data class RoutineScreenState(
    val routineName: String? = null,
    val routineStartDate: String? = null,
)