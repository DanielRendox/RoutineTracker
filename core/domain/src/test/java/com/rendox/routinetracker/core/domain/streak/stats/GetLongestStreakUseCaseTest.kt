package com.rendox.routinetracker.core.domain.streak.stats

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.getDurationInDays
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
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class GetLongestStreakUseCaseTest : KoinTest {

    private lateinit var getLongestStreak: GetLongestStreakUseCase
    private lateinit var streakRepository: StreakRepositoryFake
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
        single<StreakRepositoryFake> {
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

        getLongestStreak = GetLongestStreakUseCase(
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

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `returns cached streak if it is longest`(formCurrentStreak: Boolean) = runTest {
        val today = LocalDate(2024, 8, 5)
        val longestStreak = Streak(
            startDate = LocalDate(2024, 7, 10),
            endDate = LocalDate(2024, 7, 20),
        )
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 2),
            ),
            longestStreak,
        )

        if (formCurrentStreak) {
            val completion = Habit.YesNoHabit.CompletionRecord(date = today, completed = true)
            completionHistoryRepository.insertCompletion(habitId, completion)
        }

        streakRepository.insertStreaks(mapOf(habitId to streaks))
        assertThat(getLongestStreak(habit, today)).isEqualTo(longestStreak)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `returns current streak if it is longest`(insertCachedStreak: Boolean) = runTest {
        if (insertCachedStreak) {
            val cachedStreak = Streak(
                startDate = LocalDate(2024, 6, 1),
                endDate = LocalDate(2024, 6, 1),
            )
            streakRepository.insertStreaks(mapOf(habitId to listOf(cachedStreak)))
        }

        val today = LocalDate(2024, 7, 2)
        val completions = listOf(
            Habit.YesNoHabit.CompletionRecord(date = today.minusDays(1), completed = true),
            Habit.YesNoHabit.CompletionRecord(date = today, completed = true),
        )
        completionHistoryRepository.insertCompletions(mapOf(habitId to completions))
        val longestStreak = streakComputer.computeStreaks(
            today = today,
            habit = habit,
            completionHistory = completions,
            vacationHistory = emptyList(),
        ).maxBy { it.getDurationInDays() }
        assertThat(getLongestStreak(habit, today)).isEqualTo(longestStreak)
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
        val today = LocalDate(2024, 8, 2)
        val completions = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 8, 1),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = today,
                completed = true,
            ),
        )

        val currentPeriodStreak = streakComputer.computeStreaks(
            today = today,
            habit = habit,
            completionHistory = completions,
            vacationHistory = emptyList(),
        ).maxBy { it.startDate }
        val adjacentStreakEnd = currentPeriodStreak.startDate.minusDays(minusDays)
        val adjacentStreak = Streak(
            startDate = adjacentStreakEnd.minusDays(7),
            endDate = adjacentStreakEnd,
        )

        completionHistoryRepository.insertCompletions(mapOf(habitId to completions))
        streakRepository.insertStreaks(mapOf(habitId to listOf(adjacentStreak)))

        // if not adjacent, returns currentPeriodStreak
        val expected = listOf(adjacentStreak, currentPeriodStreak)
            .joinAdjacentStreaks()
            .maxBy { it.getDurationInDays() }
        assertThat(getLongestStreak(habit, today)).isEqualTo(expected)
    }

    @Test
    fun `caches last not cached streaks`() = runTest {
        val today = LocalDate(2024, 8, 2)
        val completions = listOf(
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 7, 1),
                completed = true,
            ),
            Habit.YesNoHabit.CompletionRecord(
                date = LocalDate(2024, 7, 2),
                completed = true,
            ),
        )
        completionHistoryRepository.insertCompletions(mapOf(habitId to completions))
        val streaks = streakComputer.computeStreaks(
            today = today,
            habit = habit,
            completionHistory = completions,
            vacationHistory = emptyList(),
        )
        // the db does not contain any streaks at the moment
        assertThat(getLongestStreak(habit, today)).isEqualTo(streaks.maxBy { it.getDurationInDays() })
    }

    @Test
    fun `returns first longest streak if multiple streaks have the same duration`() = runTest {
        val today = LocalDate(2024, 8, 1)
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 7),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 9),
                endDate = LocalDate(2024, 7, 15),
            ),
        )
        streakRepository.insertStreaks(mapOf(habitId to streaks))
        assertThat(getLongestStreak(habit, today)).isEqualTo(streaks.first())
    }

    @Test
    fun `returns null if there are no streaks`() = runTest {
        val today = LocalDate(2024, 8, 1)
        assertThat(getLongestStreak(habit, today)).isNull()
    }
}