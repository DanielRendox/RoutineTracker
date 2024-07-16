package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.agenda.GetAgendaUseCase
import com.rendox.routinetracker.core.domain.agenda.GetAgendaUseCaseImpl
import com.rendox.routinetracker.core.domain.completiondata.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completiondata.GetHabitCompletionDataUseCaseImpl
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionAndCashStreaks
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.habitstatus.HabitStatusComputer
import com.rendox.routinetracker.core.domain.habitstatus.HabitStatusComputerImpl
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
        GetHabitCompletionDataUseCaseImpl(
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