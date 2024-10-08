package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.GetStreaksInPeriodUseCase
import com.rendox.routinetracker.core.domain.streak.StreakManager
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputerImpl
import com.rendox.routinetracker.core.domain.streak.stats.GetCurrentStreakUseCase
import com.rendox.routinetracker.core.domain.streak.stats.GetLongestStreakUseCase
import org.koin.dsl.module

val streakDomainModule = module {
    single<StreakComputer> {
        StreakComputerImpl(habitStatusComputer = get())
    }

    single<StreakManager> {
        StreakManager(
            completionHistoryRepository = get(),
            vacationRepository = get(),
            streakComputer = get(),
        )
    }

    single<GetCurrentStreakUseCase> {
        GetCurrentStreakUseCase(
            streakRepository = get(),
            completionHistoryRepository = get(),
            vacationRepository = get(),
            streakComputer = get(),
        )
    }

    single<GetLongestStreakUseCase> {
        GetLongestStreakUseCase(
            streakRepository = get(),
            completionHistoryRepository = get(),
            vacationRepository = get(),
            streakComputer = get(),
        )
    }

    single<GetStreaksInPeriodUseCase> {
        GetStreaksInPeriodUseCase(
            completionHistoryRepository = get(),
            vacationRepository = get(),
            streakComputer = get(),
        )
    }
}