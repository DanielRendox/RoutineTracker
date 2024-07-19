package com.rendox.routinetracker.core.domain.streak.stats

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.joinAdjacentStreaks
import com.rendox.routinetracker.core.logic.time.minusDays
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
import org.junit.jupiter.params.provider.CsvSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class GetCurrentStreakUseCaseTest : KoinTest {

    private lateinit var getCurrentStreak: GetCurrentStreakUseCase
    private lateinit var streakRepository: StreakRepository
    private lateinit var streakComputer: StreakComputer
    private lateinit var completionHistoryRepository: CompletionHistoryRepository

    private val today = LocalDate(2024, 8, 2)
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
    private val fakeCompletions = listOf(
        Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 8, 1),
            completed = true,
        ),
        Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 8, 2),
            completed = true,
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
                testModule,
            )
        }

        streakComputer = get()
        completionHistoryRepository = get()
        streakRepository = get()
        getCurrentStreak = GetCurrentStreakUseCase(
            streakRepository = streakRepository,
            completionHistoryRepository = completionHistoryRepository,
            vacationRepository = get(),
            streakComputer = streakComputer,
        )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `computes current streak when there are no streaks in db`() = runTest {
        completionHistoryRepository.insertCompletions(mapOf(habitId to fakeCompletions))
        val expectedStreak = computeStreaks(fakeCompletions).maxBy { it.startDate }
        assertThat(getCurrentStreak(habit, today)).isEqualTo(expectedStreak)
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "joins current and cached streaks if they are adjacent, 1",
        "does not join current and cached streaks if they are not adjacent, 2",
    )
    fun `handles adjacent streaks`(
        name: String,
        minusDays: Int,
    ) = runTest {
        val currentPeriodStreak = computeStreaks(fakeCompletions).maxBy { it.startDate }
        val adjacentStreakEnd = currentPeriodStreak.startDate.minusDays(minusDays)
        val adjacentStreak = Streak(
            startDate = adjacentStreakEnd.minusDays(7),
            endDate = adjacentStreakEnd,
        )

        completionHistoryRepository.insertCompletions(mapOf(habitId to fakeCompletions))
        streakRepository.insertStreaks(mapOf(habitId to listOf(adjacentStreak)))

        // if not adjacent, returns currentPeriodStreak
        val expected = listOf(adjacentStreak, currentPeriodStreak).joinAdjacentStreaks().last()
        assertThat(getCurrentStreak(habit, today)).isEqualTo(expected)
    }

    @Test
    fun `returns null when there are no streaks in current period`() = runTest {
        assertThat(getCurrentStreak(habit, today)).isNull()
    }

    @Test
    fun `does not return streaks from different period`() = runTest {
        val streak = Streak(
            startDate = LocalDate(2024, 7, 1),
            endDate = LocalDate(2024, 7, 2),
        )
        streakRepository.insertStreaks(mapOf(habitId to listOf(streak)))
        assertThat(getCurrentStreak(habit, today)).isNull()
    }

    @Test
    fun `returns null if there is no current streak`() = runTest {
        val streak = Streak(
            startDate = LocalDate(2024, 7, 21),
            endDate = LocalDate(2024, 7, 27),
        )
        streakRepository.insertStreaks(mapOf(habitId to listOf(streak)))
        assertThat(getCurrentStreak(habit, today)).isNull()
    }

    @Test
    fun `caches last not cached streaks`() = runTest {
        val completions = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 8, 1),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 8, 2),
                completed = true,
            ),
        )
        completionHistoryRepository.insertCompletions(mapOf(habitId to completions))
        assertThat(getCurrentStreak(habit, today)).isEqualTo(computeStreaks(completions).first())
    }

    private fun computeStreaks(completions: List<Habit.CompletionRecord>): List<Streak> = streakComputer.computeStreaks(
        today = today,
        habit = habit,
        completionHistory = completions,
        vacationHistory = emptyList(),
    )
}