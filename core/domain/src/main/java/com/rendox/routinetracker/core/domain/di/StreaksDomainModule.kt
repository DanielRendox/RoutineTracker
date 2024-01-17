package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetAllStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.GetAllStreaksWithCashingUseCase
import com.rendox.routinetracker.core.domain.streak.StreakComputer
import com.rendox.routinetracker.core.domain.streak.StreakComputerImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val streakDomainModule = module {
    single<StreakComputer> {
        StreakComputerImpl(habitStatusComputer = get())
    }

    single<GetAllStreaksUseCase> {
//        GetAllStreaksUseCaseImpl(
//            getHabit = get(),
//            completionHistoryRepository = get(),
//            vacationHistoryRepository = get(),
//            defaultDispatcher = get(qualifier = named("defaultDispatcher")),
//            streakComputer = get(),
//        )
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