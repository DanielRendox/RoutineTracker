package com.rendox.routinetracker

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val routineModule = module {
    viewModel {parameters ->
        RoutineViewModel(
            routineRepository = get(),
            routineId = parameters[0],
        )
    }
}