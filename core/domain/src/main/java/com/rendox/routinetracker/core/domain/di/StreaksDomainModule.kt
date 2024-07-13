package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.GetAllStreaksWithCashingUseCase
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputerImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val streakDomainModule = module {
    single<StreakComputer> {
        StreakComputerImpl(habitStatusComputer = get())
    }

    single<GetAllStreaksUseCase> {
        GetAllStreaksWithCashingUseCase(
            getHabit = get(),
            completionHistoryRepository = get(),
            vacationHistoryRepository = get(),
            defaultDispatcher = get(qualifier = named("defaultDispatcher")),
            streakComputer = get(),
            streakRepository = get(),
        )
    }
}