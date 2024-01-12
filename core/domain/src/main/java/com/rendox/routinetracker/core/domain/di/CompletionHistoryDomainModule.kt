package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        InsertHabitCompletionUseCase(
            completionHistoryRepository = get(),
            habitRepository = get(),
        )
    }
}