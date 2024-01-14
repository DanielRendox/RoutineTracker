package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCaseImpl
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCaseImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single<InsertHabitCompletionUseCase> {
        InsertHabitCompletionUseCaseImpl(
            completionHistoryRepository = get(),
            getHabit = get(),
        )
    }

    single<GetHabitCompletionDataUseCase> {
        GetHabitCompletionDataUseCaseImpl(
            getHabit = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
            defaultDispatcher = get(qualifier = named("defaultDispatcher")),
        )
    }
}