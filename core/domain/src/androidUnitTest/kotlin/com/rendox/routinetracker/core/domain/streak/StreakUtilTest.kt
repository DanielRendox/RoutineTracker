package com.rendox.routinetracker.core.domain.streak

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class StreakUtilTest {

    private val streaks = listOf(
        Streak(
            startDate = LocalDate(2023, 1, 5),
            endDate = LocalDate(2023, 1, 7),
        ),
        Streak(
            startDate = LocalDate(2023, 1, 11),
            endDate = LocalDate(2023, 1, 20),
        ),
        Streak(
            startDate = LocalDate(2023, 3, 1),
            endDate = LocalDate(2023, 3, 5),
        ),
        Streak(
            startDate = LocalDate(2023, 4, 1),
            endDate = LocalDate(2023, 4, 10),
        ),
    )

    @Test
    fun assertStreakContainsDateWithinStartAndEndDates() {
        assertThat(
            streaks.first().contains(LocalDate(2023, 1, 6))
        ).isTrue()
    }
    
    @Test
    fun assertStreakDoesNotContainDateThatIsNotWithinDateRange() {
        assertThat(
            streaks[3].contains(date = LocalDate(2023, 4, 11))
        ).isFalse()
    }

    @Test
    fun getDurationOfStreakTest() {
        assertThat(streaks.last().getDurationInDays()).isEqualTo(10)
    }

    @Test
    fun getCurrentStreakTest() {
        assertThat(
            streaks.getCurrentStreak(LocalDate(2023, 4, 11))
        ).isEqualTo(streaks.last())

        assertThat(
            streaks.take(3).getCurrentStreak(LocalDate(2023, 3, 5))
        ).isEqualTo(streaks[2])
    }

    @Test
    fun getLongestStreakTest() {
        assertThat(streaks.getLongestStreak()).isEqualTo(streaks[1])
    }

    @Test
    fun `joinAdjacentStreaks joins only adjacent streaks and keeps others intact`() {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 10),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 11),
                endDate = LocalDate(2022, 1, 20),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 25),
                endDate = LocalDate(2022, 1, 30),
            ),
        )
        assertThat(streaks.joinAdjacentStreaks()).containsExactly(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 20),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 25),
                endDate = LocalDate(2022, 1, 30),
            ),
        )
    }

    @Test
    fun `joinAdjacent streaks joins all streaks into one if they are adjacent`() {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 10),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 11),
                endDate = LocalDate(2022, 1, 20),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 21),
                endDate = LocalDate(2022, 1, 30),
            ),
        )
        assertThat(streaks.joinAdjacentStreaks()).containsExactly(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 30),
            )
        )
    }

    @Test
    fun `joinAdjacent streaks returns the same list when non of the streaks are adjacent`() {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 10),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 15),
                endDate = LocalDate(2022, 1, 20),
            ),
            Streak(
                startDate = LocalDate(2022, 1, 25),
                endDate = LocalDate(2022, 1, 30),
            ),
        )
        assertThat(streaks.joinAdjacentStreaks()).containsExactlyElementsIn(streaks)
    }

    @Test
    fun `joinAdjacentStreaks returns the same list when given a single streak list`() {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 10),
            )
        )
        assertThat(streaks.joinAdjacentStreaks()).containsExactlyElementsIn(streaks)
    }

    @Test
    fun `joins adjacent streaks with same start and end date`() {
        val streak1 = Streak(
            startDate = LocalDate(2022, 1, 1),
            endDate = LocalDate(2022, 1, 1),
        )
        val streak2 = Streak(
            startDate = LocalDate(2022, 1, 2),
            endDate = LocalDate(2022, 1, 2),
        )
        val streaks = listOf(streak1, streak2)
        assertThat(streaks.joinAdjacentStreaks()).containsExactly(
            Streak(
                startDate = LocalDate(2022, 1, 1),
                endDate = LocalDate(2022, 1, 2),
            )
        )
    }
}