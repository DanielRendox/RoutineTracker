package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCaseIndependentPeriods
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputerImpl
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionAndCashStreaks
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionHistoryDomainModule = module {
    single<HabitStatusComputer> { HabitStatusComputerImpl() }

    single<InsertHabitCompletionUseCase> {
        InsertHabitCompletionAndCashStreaks(
            completionHistoryRepository = get(),
            vacationRepository = get(),
            getHabit = get(),
            streakComputer = get(),
            streakRepository = get(),
        )
    }

    single<GetHabitCompletionDataUseCase> {
        GetHabitCompletionDataUseCaseIndependentPeriods(
            getHabit = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
            habitStatusComputer = get(),
            defaultDispatcher = get(qualifier = named("defaultDispatcher")),
        )
    }
}