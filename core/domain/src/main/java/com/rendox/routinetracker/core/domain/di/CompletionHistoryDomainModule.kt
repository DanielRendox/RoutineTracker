package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.agenda.GetAgendaUseCase
import com.rendox.routinetracker.core.domain.agenda.GetAgendaUseCaseImpl
import com.rendox.routinetracker.core.domain.completion_data.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completion_data.GetHabitCompletionDataUseCaseIndependentPeriods
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionAndCashStreaks
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.habit_status.HabitStatusComputer
import com.rendox.routinetracker.core.domain.habit_status.HabitStatusComputerImpl
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

    single<GetAgendaUseCase> {
        GetAgendaUseCaseImpl(
            habitRepository = get(),
            vacationRepository = get(),
            completionHistoryRepository = get(),
            habitStatusComputer = get(),
        )
    }
}