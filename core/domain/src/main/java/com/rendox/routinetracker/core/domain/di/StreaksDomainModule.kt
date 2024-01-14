package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCaseImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val streaksDomainModule = module {
    single<GetAllStreaksUseCase> {
        GetAllStreaksUseCaseImpl(
            getHabit = get(),
            completionHistoryRepository = get(),
            vacationHistoryRepository = get(),
            defaultDispatcher = get(qualifier = named("defaultDispatcher")),
        )
    }
}