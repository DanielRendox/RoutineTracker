package com.rendox.routinetracker.routine_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.model.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineDetailsViewModel(
    routineId: Long,
    routineRepository: RoutineRepository,
) : ViewModel() {

    private val _routineFlow: MutableStateFlow<Routine?> = MutableStateFlow(null)
    val routineFlow = _routineFlow.asStateFlow()

    init {
        viewModelScope.launch {
            _routineFlow.update { routineRepository.getRoutineById(routineId) }
        }
    }
}