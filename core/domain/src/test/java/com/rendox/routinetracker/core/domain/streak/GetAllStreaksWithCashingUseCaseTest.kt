package com.rendox.routinetracker.core.domain.streak

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import com.rendox.routinetracker.core.testcommon.fakes.habit.CompletionHistoryRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitData
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.StreakRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.VacationRepositoryFake
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class GetAllStreaksWithCashingUseCaseTest : KoinTest {

    private lateinit var getAllStreaksUseCase: GetAllStreaksUseCase
    private lateinit var streakRepository: StreakRepository
    private lateinit var habitRepository: HabitRepository
    private val habitId: Long = 1L

    private val defaultHabit = Habit.YesNoHabit(
        id = habitId,
        name = "Test habit",
        schedule = Schedule.EveryDaySchedule(
            startDate = LocalDate(2023, 11, 1),
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
        single<StreakComputer> {
            object : StreakComputer {
                override fun computeStreaks(
                    today: LocalDate,
                    habit: Habit,
                    completionHistory: List<Habit.CompletionRecord>,
                    vacationHistory: List<Vacation>,
                ): List<Streak> = fakeStreaks.filter { it.endDate <= today }
            }
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

        streakRepository = get<StreakRepository>()
        habitRepository = get<HabitRepository>()
        habitRepository.insertHabit(defaultHabit)

        getAllStreaksUseCase = GetAllStreaksWithCashingUseCase(
            getHabit = get(),
            streakRepository = get(),
            streakComputer = get(),
            defaultDispatcher = get(),
            completionHistoryRepository = get(),
            vacationHistoryRepository = get(),
        )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `computes streaks when there are no cashed streaks`() = runTest {
        val streaks = getAllStreaksUseCase(
            habitId = habitId,
            today = LocalDate(2024, 2, 28),
        )
        assertThat(streaks).containsExactlyElementsIn(fakeStreaks)
    }

    @Test
    fun `does not compute streaks for past periods if they are cashed`() = runTest {
        streakRepository.insertStreaks(
            streaks = cashedStreaks.map { habitId to it },
            periods = listOf(
                LocalDateRange(
                    start = LocalDate(2023, 11, 1),
                    endInclusive = LocalDate(2023, 11, 30),
                ),
            ).map { habitId to it },
        )
        val streaks = getAllStreaksUseCase(
            habitId = habitId,
            today = LocalDate(2023, 12, 31),
        )
        val novemberCashedStreaks = cashedStreaks.take(1)
        val decemberComputedStreaks = fakeStreaks.take(4)
        val expectedStreaks = novemberCashedStreaks + decemberComputedStreaks
        assertThat(streaks).containsExactlyElementsIn(expectedStreaks)
    }

    @Test
    fun `computes and cashes previous period streaks if they are not cashed already`() = runTest {
        getAllStreaksUseCase(
            habitId = habitId,
            today = LocalDate(2024, 1, 31),
        )
        val newlySavedStreaksExpected = fakeStreaks.take(5)
        val newlySavedStreaks = streakRepository.getAllStreaks(habitId)
        assertThat(newlySavedStreaks).containsExactlyElementsIn(newlySavedStreaksExpected)
    }

    @Test
    fun `computes and cashes end date period streaks if they are not cashed already`() = runTest {
        val habitId = 2L
        val habit = defaultHabit.copy(
            id = habitId,
            schedule = Schedule.EveryDaySchedule(
                startDate = LocalDate(2023, 11, 1),
                endDate = LocalDate(2023, 12, 31),
            ),
        )
        habitRepository.insertHabit(habit)
        getAllStreaksUseCase(
            habitId = habitId,
            today = LocalDate(2024, 1, 31),
        )
        val newlySavedStreaksExpected = fakeStreaks.take(5)
        val newlySavedStreaks = streakRepository.getAllStreaks(habitId)
        assertThat(newlySavedStreaks).containsExactlyElementsIn(newlySavedStreaksExpected)
    }

    companion object {
        val fakeStreaks = listOf(
            Streak(
                startDate = LocalDate(2023, 12, 1),
                endDate = LocalDate(2023, 12, 2),
            ),
            Streak(
                startDate = LocalDate(2023, 12, 5),
                endDate = LocalDate(2023, 12, 10),
            ),
            Streak(
                startDate = LocalDate(2023, 12, 12),
                endDate = LocalDate(2023, 12, 17),
            ),
            Streak(
                startDate = LocalDate(2023, 12, 22),
                endDate = LocalDate(2023, 12, 25),
            ),
            Streak(
                startDate = LocalDate(2023, 12, 27),
                endDate = LocalDate(2024, 1, 6),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 8),
                endDate = LocalDate(2024, 1, 14),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 21),
                endDate = LocalDate(2024, 1, 21),
            ),
            Streak(
                startDate = LocalDate(2024, 1, 23),
                endDate = LocalDate(2024, 2, 20),
            ),
        )

        val cashedStreaks = listOf(
            Streak(
                startDate = LocalDate(2023, 11, 10),
                endDate = LocalDate(2023, 11, 15),
            ),
        )
    }
}