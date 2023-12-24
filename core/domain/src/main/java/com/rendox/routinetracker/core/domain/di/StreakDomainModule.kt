package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.streak.BreakStreakUseCase
import com.rendox.routinetracker.core.domain.streak.ContinueStreakIfEndedUseCase
import com.rendox.routinetracker.core.domain.streak.DeleteStreakIfStartedUseCase
import com.rendox.routinetracker.core.domain.streak.GetDisplayStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import org.koin.dsl.module

val streakDomainModule = module {
    single {
        BreakStreakUseCase(
            routineCompletionHistoryRepository = get(),
            streakRepository = get(),
        )
    }

    single {
        ContinueStreakIfEndedUseCase(
            streakRepository = get()
        )
    }

    single {
        DeleteStreakIfStartedUseCase(
            streakRepository = get()
        )
    }

    single {
        StartStreakOrJoinStreaksUseCase(
            streakRepository = get(),
            routineCompletionHistoryRepository = get(),
        )
    }

    single {
        GetDisplayStreaksUseCase(
            streakRepository = get(),
            routineCompletionHistoryRepository = get(),
            habitRepository = get(),
        )
    }
}