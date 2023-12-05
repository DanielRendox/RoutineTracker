package com.rendox.routinetracker.routine_details.di

import com.rendox.routinetracker.routine_details.RoutineDetailsViewModel
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val routineDetailsModule = module {
    viewModel { parameters ->
        RoutineCalendarViewModel(
            routineId = parameters.get(),
            routineRepository = get(),
            getRoutineStatusList = get(),
            insertRoutineStatus = get(),
            toggleRoutineStatus = get(),
            getAllStreaks = get(),
        )
    }
    viewModel { parameters ->
        RoutineDetailsViewModel(
            routineId = parameters.get(),
            routineRepository = get(),
        )
    }
}