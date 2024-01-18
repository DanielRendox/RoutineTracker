package com.rendox.routinetracker.routine_details.di

import com.rendox.routinetracker.routine_details.RoutineDetailsScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val routineDetailsModule = module {
    viewModel { parameters ->
        RoutineDetailsScreenViewModel(
            routineId = parameters.get(),
            getHabit = get(),
            getAllStreaksUseCase = get(),
            insertHabitCompletion = get(),
            getHabitCompletionData = get(),
            deleteHabit = get(),
        )
    }
}