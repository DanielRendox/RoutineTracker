package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_time.GetHabitCompletionTimeUseCase
import org.koin.dsl.module

val habitDomainModule = module {

    single {
        GetHabitCompletionTimeUseCase(
            habitRepository = get(),
            completionTimeRepository = get(),
        )
    }

    single {
        HabitComputeStatusUseCase(
            habitRepository = get(),
            completionHistoryRepository = get(),
            vacationRepository = get(),
        )
    }
}