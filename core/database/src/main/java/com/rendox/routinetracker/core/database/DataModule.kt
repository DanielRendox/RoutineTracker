package com.rendox.routinetracker.core.database

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.database.schedule.DueDateEntity
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.schedule.WeekDayMonthRelatedEntity
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.epoch
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.koin.dsl.module

val dataModule = module {

    single {
        Dispatchers.IO
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = RoutineTrackerDatabase.Schema,
            context = get(),
        )
    }

    single {
        RoutineTrackerDatabase(
            driver = get(),
            routineEntityAdapter = RoutineEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
            ),
            scheduleEntityAdapter = ScheduleEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
                startDateAdapter = localDateAdapter,
                vacationStartDateAdapter = localDateAdapter,
                vacationEndDateAdapter = localDateAdapter,
                numOfDaysInPeriodicScheduleAdapter = IntColumnAdapter,
                startDayOfWeekInWeeklyScheduleAdapter = dayOfWeekAdapter,
                startDayOfYearInAnnualScheduleAdapter = annualDateAdapter,
                scheduleDeviationAdapter = IntColumnAdapter,
            ),
            dueDateEntityAdapter = DueDateEntity.Adapter(
                dueDateNumberAdapter = IntColumnAdapter,
            ),
            weekDayMonthRelatedEntityAdapter = WeekDayMonthRelatedEntity.Adapter(
                weekDayIndexAdapter = IntColumnAdapter,
                weekDayNumberMonthRelatedAdapter = EnumColumnAdapter(),
            ),
            completionHistoryEntityAdapter = CompletionHistoryEntity.Adapter(
                statusAdapter = EnumColumnAdapter(),
            )
        )
    }
}

val dayOfWeekAdapter = object : ColumnAdapter<DayOfWeek, Long> {
    override fun decode(databaseValue: Long) = databaseValue.toInt().toDayOfWeek()
    override fun encode(value: DayOfWeek) = value.toInt().toLong()
}

fun Int.toDayOfWeek() = DayOfWeek(this)
fun DayOfWeek.toInt() = this.value

val localDateAdapter = object : ColumnAdapter<LocalDate, Long> {
    override fun decode(databaseValue: Long) = databaseValue.toInt().toLocalDate()
    override fun encode(value: LocalDate) = value.toInt().toLong()
}

fun Int.toLocalDate() = epoch.plus(DatePeriod(days = this))
fun LocalDate.toInt() = epoch.daysUntil(this)

val annualDateAdapter = object : ColumnAdapter<AnnualDate, Long> {
    override fun decode(databaseValue: Long) = databaseValue.toInt().toAnnualDate()
    override fun encode(value: AnnualDate) = value.toInt().toLong()
}

fun Int.toAnnualDate(): AnnualDate {
    if (this == 366) {
        return AnnualDate(Month.FEBRUARY, 29)
    }
    val arbitraryNotLeapYearStart = epoch
    val requestedDate = arbitraryNotLeapYearStart.plus(DatePeriod(days = this))
    return AnnualDate(requestedDate.month, requestedDate.dayOfMonth)
}

fun AnnualDate.toInt(): Int {
    if (this == AnnualDate(Month.FEBRUARY, 29)) {
        return 366
    }
    val arbitraryNotLeapYearStart = epoch
    val requestedDate = LocalDate(arbitraryNotLeapYearStart.year, this.month, this.dayOfMonth)
    return arbitraryNotLeapYearStart.daysUntil(requestedDate)
}