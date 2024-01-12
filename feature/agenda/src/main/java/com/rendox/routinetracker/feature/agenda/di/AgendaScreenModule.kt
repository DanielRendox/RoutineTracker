package com.rendox.routinetracker.feature.agenda.di

import com.rendox.routinetracker.feature.agenda.AgendaScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val agendaScreenModule = module {

    viewModel {
        AgendaScreenViewModel(
            habitRepository = get(),
            insertHabitCompletion = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
        )
    }
}