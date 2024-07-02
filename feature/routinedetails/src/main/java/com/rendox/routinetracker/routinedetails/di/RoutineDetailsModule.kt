package com.rendox.routinetracker.routinedetails.di

import com.rendox.routinetracker.routinedetails.RoutineDetailsScreenViewModel
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