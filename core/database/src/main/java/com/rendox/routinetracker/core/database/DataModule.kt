package com.rendox.routinetracker.core.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.logic.time.epoch
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.koin.dsl.module

val dataModule = module {

    single {
        Dispatchers.IO
    }

    single {
        RoutineTrackerDatabase(
            driver = AndroidSqliteDriver(
                schema = RoutineTrackerDatabase.Schema,
                context = get(),
            ),
            routineEntityAdapter = RoutineEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
                startDateAdapter = localDateAdapter,
                vacationStartDateAdapter = localDateAdapter,
                vacationEndDateAdapter = localDateAdapter,
            )
        )
    }
}

val localDateAdapter = object : ColumnAdapter<LocalDate, Long> {
    override fun decode(databaseValue: Long): LocalDate =
        epoch.plus(DatePeriod(days = databaseValue.toInt()))

    override fun encode(value: LocalDate): Long =
        epoch.daysUntil(value).toLong()
}