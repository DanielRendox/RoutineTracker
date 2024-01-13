package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCase
import org.koin.dsl.module

val streaksDomainModule = module {
    single {
        GetAllStreaksUseCase(
            habitRepository = get(),
            completionHistoryRepository = get(),
            habitComputeStatusUseCase = get(),
            vacationHistoryRepository = get(),
        )
    }
}