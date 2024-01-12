package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCase
import org.koin.dsl.module

val streakDomainModule = module {
    single {
        GetAllStreaksUseCase(
            completionHistoryRepository = get(),
            computeHabitStatus = get(),
            habitRepository = get(),
        )
    }
}