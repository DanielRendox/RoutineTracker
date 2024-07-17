package com.rendox.routinetracker.core.domain.completionhistory

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.testcommon.fakes.habit.CompletionHistoryRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitData
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.StreakRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.VacationRepositoryFake
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class InsertHabitCompletionAndCashStreaksTest : KoinTest {

    private lateinit var insertHabitCompletionAndCashStreaks: InsertHabitCompletionAndCashStreaks
    private lateinit var streakRepository: StreakRepository
    private lateinit var streakComputer: StreakComputer
    private lateinit var completionHistoryRepository: CompletionHistoryRepository
    private val habitId = 1L
    private val habit = Habit.YesNoHabit(
        id = habitId,
        name = "Test Habit",
        schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            startDate = LocalDate(2024, 1, 1),
            startDayOfWeek = DayOfWeek.MONDAY,
            numOfDueDays = 2,
        ),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testModule = module {
        single { HabitData() }
        single<CoroutineContext> {
            UnconfinedTestDispatcher()
        }
        single<HabitRepository> {
            HabitRepositoryFake(habitData = get())
        }
        single<StreakRepository> {
            StreakRepositoryFake(habitData = get())
        }
        single<VacationRepository> {
            VacationRepositoryFake(habitData = get())
        }
        single<CompletionHistoryRepository> {
            CompletionHistoryRepositoryFake(habitData = get())
        }
    }

    @BeforeEach
    fun setUp() = runTest {
        startKoin {
            modules(
                streakDomainModule,
                completionHistoryDomainModule,
                habitDomainModule,
                streakDomainModule,
                testModule,
            )
        }

        val habitRepository: HabitRepository = get()
        habitRepository.insertHabit(habit)

        insertHabitCompletionAndCashStreaks = InsertHabitCompletionAndCashStreaks(
            completionHistoryRepository = get(),
            getHabit = get(),
            streakManager = get(),
        )

        streakRepository = get()
        streakComputer = get()
        completionHistoryRepository = get()
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `caches streak when completed in past period`() = runTest {
        val completedDate = LocalDate(2024, 7, 1)
        val today = LocalDate(2024, 7, 14)
        val completionRecord = Habit.YesNoHabit.CompletionRecord(
            date = completedDate,
            completed = true,
        )
        insertHabitCompletionAndCashStreaks(habitId, completionRecord, today)
        val expected = computeStreaks(completions = listOf(completionRecord), today = today)
        val actual = streakRepository.getAllStreaks(habitId)
        assertThat(actual).containsExactlyElementsIn(expected)
    }

    @Test
    fun `does not cache streak when completed in current period`() = runTest {
        val completedDate = LocalDate(2024, 7, 1)
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            ),
            today = completedDate,
        )
        assertThat(streakRepository.getAllStreaks(habitId)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `caches all streaks accept ones in current period`(completed: Boolean) = runTest {
        val firstCompletion = Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 7, 1),
            completed = completed,
        )
        val secondCompletion = Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 7, 8),
            completed = true,
        )
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = firstCompletion,
            today = secondCompletion.date,
        )
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = secondCompletion,
            today = secondCompletion.date,
        )
        val firstStreak = computeStreaks(completions = listOf(firstCompletion), today = secondCompletion.date)
        assertThat(streakRepository.getAllStreaks(habitId)).containsExactlyElementsIn(firstStreak)
    }

    @Test
    fun `caches last not cached streak`() = runTest {
        val firstCompletion = Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 7, 1),
            completed = true,
        )
        val secondCompletion = Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 7, 8),
            completed = true,
        )
        // doesn't cache streaks for current period
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = firstCompletion,
            today = firstCompletion.date,
        )

        // should cache streaks for last not saved period
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = secondCompletion,
            today = secondCompletion.date,
        )
        val expected = computeStreaks(
            completions = listOf(firstCompletion),
            today = secondCompletion.date,
        )
        val actual = streakRepository.getAllStreaks(habitId)
        assertThat(actual).containsAtLeastElementsIn(expected)
    }

    @Test
    fun `caches multiple not cashed streaks`() = runTest {
        val completions: List<Habit.CompletionRecord> = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 6, 1),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 7, 1),
                completed = true,
            ),
        )
        completionHistoryRepository.insertCompletions(mapOf(habitId to completions))

        val today = LocalDate(2024, 8, 1)
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = today,
                completed = false,
            ),
            today = today,
        )

        val expected = computeStreaks(completions, today)
        assertThat(streakRepository.getAllStreaks(habitId)).containsExactlyElementsIn(expected)
    }

    @Test
    fun `deletes streaks when uncompleted in past period`() = runTest {
        val completedDate = LocalDate(2024, 7, 1)
        val today = LocalDate(2024, 7, 14)
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = true,
            ),
            today = today,
        )
        // undo completion
        insertHabitCompletionAndCashStreaks(
            habitId = habitId,
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = completedDate,
                completed = false,
            ),
            today = today,
        )
        assertThat(streakRepository.getAllStreaks(habitId)).isEmpty()
    }

    private fun computeStreaks(
        completions: List<Habit.CompletionRecord>,
        today: LocalDate,
    ): List<Streak> = streakComputer.computeStreaks(
        today = today,
        habit = habit,
        completionHistory = completions,
        vacationHistory = emptyList(),
    )
}