package com.rendox.routinetracker.routine_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineDetailsViewModel(
    routineId: Long,
    routineRepository: RoutineRepository,
) : ViewModel() {

    private val _habitFlow: MutableStateFlow<Habit?> = MutableStateFlow(null)
    val routineFlow = _habitFlow.asStateFlow()

    init {
        viewModelScope.launch {
            _habitFlow.update { routineRepository.getRoutineById(routineId) }
        }
    }
}