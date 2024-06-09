package com.rendox.routinetracker.core.database.di

import com.rendox.routinetracker.core.database.habit.DueDateLocalDataSource
import com.rendox.routinetracker.core.database.habit.DueDateLocalDataSourceImpl
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSourceImpl
import com.rendox.routinetracker.core.database.habit.ScheduleLocalDataSource
import com.rendox.routinetracker.core.database.habit.ScheduleLocalDataSourceImpl
import com.rendox.routinetracker.core.database.habit.WeekDaysMonthRelatedLocalDataSource
import com.rendox.routinetracker.core.database.habit.WeekDaysMonthRelatedLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val habitLocalDataModule = module {
    single<DueDateLocalDataSource> {
        DueDateLocalDataSourceImpl(
            db = get(),
        )
    }

    single<WeekDaysMonthRelatedLocalDataSource> {
        WeekDaysMonthRelatedLocalDataSourceImpl(
            db = get(),
        )
    }

    single<ScheduleLocalDataSource> {
        ScheduleLocalDataSourceImpl(
            db = get(),
            dueDateLocalDataSource = get(),
            weekDaysMonthRelatedLocalDataSource = get(),
        )
    }

    single<HabitLocalDataSource> {
        HabitLocalDataSourceImpl(
            db = get(),
            scheduleLocalDataSource = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
}