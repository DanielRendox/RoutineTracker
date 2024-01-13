package com.rendox.routinetracker.core.domain.streak

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class StreakUtilTest {

    private val streaks = listOf(
        DisplayStreak(
            startDate = LocalDate(2023, 1, 5),
            endDate = LocalDate(2023, 1, 7),
        ),
        DisplayStreak(
            startDate = LocalDate(2023, 1, 11),
            endDate = LocalDate(2023, 1, 20),
        ),
        DisplayStreak(
            startDate = LocalDate(2023, 3, 1),
            endDate = LocalDate(2023, 3, 5),
        ),
        DisplayStreak(
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
}