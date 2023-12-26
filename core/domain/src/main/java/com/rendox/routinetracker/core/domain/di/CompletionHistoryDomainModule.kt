package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        HabitComputeStatusUseCase(
            habitRepository = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
        )
    }

    single {
        InsertHabitCompletionUseCase(
            completionHistoryRepository = get(),
            habitRepository = get(),
        )
    }
}