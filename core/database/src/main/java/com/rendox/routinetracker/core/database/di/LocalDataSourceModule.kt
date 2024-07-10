package com.rendox.routinetracker.core.database.di

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.adapter.primitive.FloatColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rendox.routinetracker.core.database.CachedStreakEntity
import com.rendox.routinetracker.core.database.CompletionHistoryEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.SpecificDateCustomCompletionTime
import com.rendox.routinetracker.core.database.VacationEntity
import com.rendox.routinetracker.core.database.habit.HabitEntity
import com.rendox.routinetracker.core.database.schedule.DueDateEntity
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.schedule.WeekDayMonthRelatedEntity
import com.rendox.routinetracker.core.logic.time.epochDate
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.model.AnnualDate
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import org.koin.core.qualifier.named
import org.koin.dsl.module

val localDataSourceModule = module {
    single<CoroutineContext>(named("ioDispatcher")) {
        Dispatchers.IO
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = RoutineTrackerDatabase.Schema,
            context = get(),
            name = "routinetracker.db",
            callback = object : AndroidSqliteDriver.Callback(RoutineTrackerDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
        )
    }

    single {
        RoutineTrackerDatabase(
            driver = get(),
            habitEntityAdapter = HabitEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
                sessionDurationMinutesAdapter = IntColumnAdapter,
                progressAdapter = FloatColumnAdapter,
                defaultCompletionTimeHourAdapter = IntColumnAdapter,
                defaultCompletionTimeMinuteAdapter = IntColumnAdapter,
            ),
            scheduleEntityAdapter = ScheduleEntity.Adapter(
                typeAdapter = EnumColumnAdapter(),
                startDateAdapter = localDateAdapter,
                endDateAdapter = localDateAdapter,
                startDayOfWeekInWeeklyScheduleAdapter = dayOfWeekAdapter,
                numOfDueDaysInByNumOfDueDaysScheduleAdapter = IntColumnAdapter,
                numOfDueDaysInFirstPeriodInByNumOfDueDaysScheduleAdapter = IntColumnAdapter,
                numOfDaysInAlternateDaysScheduleAdapter = IntColumnAdapter,
            ),
            dueDateEntityAdapter = DueDateEntity.Adapter(
                dueDateNumberAdapter = IntColumnAdapter,
                completionTimeMinuteAdapter = IntColumnAdapter,
                completionTimeHourAdapter = IntColumnAdapter,
            ),
            weekDayMonthRelatedEntityAdapter = WeekDayMonthRelatedEntity.Adapter(
                weekDayIndexAdapter = IntColumnAdapter,
                weekDayNumberMonthRelatedAdapter = EnumColumnAdapter(),
            ),
            specificDateCustomCompletionTimeAdapter = SpecificDateCustomCompletionTime.Adapter(
                dateAdapter = localDateAdapter,
                completionTimeHourAdapter = IntColumnAdapter,
                completionTimeMinuteAdapter = IntColumnAdapter,
            ),
            completionHistoryEntityAdapter = CompletionHistoryEntity.Adapter(
                dateAdapter = localDateAdapter,
                numOfTimesCompletedAdapter = FloatColumnAdapter,
            ),
            vacationEntityAdapter = VacationEntity.Adapter(
                startDateAdapter = localDateAdapter,
                endDateAdapter = localDateAdapter,
            ),
            cachedStreakEntityAdapter = CachedStreakEntity.Adapter(
                startDateAdapter = localDateAdapter,
                endDateAdapter = localDateAdapter,
            ),
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

fun Int.toLocalDate() = epochDate.plusDays(this)
fun LocalDate.toInt() = epochDate.daysUntil(this)

fun Int.toAnnualDate(): AnnualDate {
    if (this == 366) {
        return AnnualDate(Month.FEBRUARY, 29)
    }
    val arbitraryNotLeapYearStart = epochDate
    val requestedDate = arbitraryNotLeapYearStart.plusDays(this)
    return AnnualDate(requestedDate.month, requestedDate.dayOfMonth)
}

fun AnnualDate.toInt(): Int {
    if (this == AnnualDate(Month.FEBRUARY, 29)) {
        return 366
    }
    val arbitraryNotLeapYearStart = epochDate
    val requestedDate = LocalDate(arbitraryNotLeapYearStart.year, this.month, this.dayOfMonth)
    return arbitraryNotLeapYearStart.daysUntil(requestedDate)
}