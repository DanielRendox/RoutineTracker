package com.rendox.routinetracker

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rendox.performancetracker.Database
import com.rendox.performancetracker.feature.routine.Routine
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
        Database(
            driver = AndroidSqliteDriver(
                schema = Database.Schema,
                context = get(),
            ),
            routineAdapter = Routine.Adapter(
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