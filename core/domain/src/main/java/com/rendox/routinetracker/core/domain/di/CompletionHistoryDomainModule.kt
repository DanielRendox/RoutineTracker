package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        InsertRoutineStatusUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
            startStreakOrJoinStreaks = get(),
            breakStreak = get(),
        )
    }

    single {
        GetRoutineStatusUseCase(
            routineRepository = get(),
            completionHistoryRepository = get(),
            insertRoutineStatus = get(),
        )
    }

    single {
        ToggleHistoricalStatusUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
            startStreakOrJoinStreaks = get(),
            breakStreak = get(),
            deleteStreakIfStarted = get(),
            continueStreakIfEnded = get(),
        )
    }
}