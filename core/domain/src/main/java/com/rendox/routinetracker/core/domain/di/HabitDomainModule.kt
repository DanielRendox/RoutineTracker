package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.model.Habit
import org.koin.dsl.module

val habitDomainModule = module {
    single {
        InsertHabitUseCase(get<HabitRepository>()::insertHabit)
    }

    single {
        GetHabitUseCase(get<HabitRepository>()::getHabitById)
    }

    single {
        DeleteHabitUseCase(get<HabitRepository>()::deleteHabit)
    }
}

fun interface InsertHabitUseCase : suspend (Habit) -> Unit
fun interface GetHabitUseCase : suspend (Long) -> Habit
fun interface DeleteHabitUseCase : suspend (Long) -> Unit