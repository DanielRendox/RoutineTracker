package com.rendox.routinetracker.routine_details.di

import com.rendox.routinetracker.routine_details.RoutineDetailsViewModel
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val routineDetailsModule = module {
    viewModel { parameters ->
        RoutineCalendarViewModel(
            routineId = parameters.get(),
            habitRepository = get(),
            computeHabitStatus = get(),
            completionHistoryRepository = get(),
            insertHabitCompletion = get(),
        )
    }
    viewModel { parameters ->
        RoutineDetailsViewModel(
            routineId = parameters.get(),
            habitRepository = get(),
        )
    }
}