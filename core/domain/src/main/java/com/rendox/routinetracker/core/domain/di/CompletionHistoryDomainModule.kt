package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        InsertRoutineStatusUseCase(
            routineCompletionHistoryRepository = get(),
            habitRepository = get(),
            startStreakOrJoinStreaks = get(),
            breakStreak = get(),
        )
    }

    single {
        GetRoutineStatusUseCase(
            habitRepository = get(),
            routineCompletionHistoryRepository = get(),
            insertRoutineStatus = get(),
        )
    }

    single {
        ToggleHistoricalStatusUseCase(
            routineCompletionHistoryRepository = get(),
            habitRepository = get(),
            startStreakOrJoinStreaks = get(),
            breakStreak = get(),
            deleteStreakIfStarted = get(),
            continueStreakIfEnded = get(),
        )
    }
}