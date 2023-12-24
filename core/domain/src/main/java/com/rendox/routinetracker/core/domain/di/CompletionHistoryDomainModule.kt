package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        HabitComputeStatusUseCase(
            habitRepository = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
        )
    }
}