package com.rendox.routinetracker.core.domain.streak

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.LocalDate
import org.junit.Test

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

    @Test
    fun mapDateToInclusionStatusInDateRangeTest() {
        val startDate = LocalDate(2023, 1, 1)
        val endDate = LocalDate(2023, 1, 21)

        val expectedMap = mapOf(
            LocalDate(2023, 1, 1) to false,
            LocalDate(2023, 1, 2) to false,
            LocalDate(2023, 1, 3) to false,
            LocalDate(2023, 1,4) to false,

            LocalDate(2023, 1, 5) to true,
            LocalDate(2023, 1, 6) to true,
            LocalDate(2023, 1, 7) to true,

            LocalDate(2023, 1, 8) to false,
            LocalDate(2023, 1, 9) to false,
            LocalDate(2023, 1, 10) to false,

            LocalDate(2023, 1,11) to true,
            LocalDate(2023, 1, 12) to true,
            LocalDate(2023, 1, 13) to true,
            LocalDate(2023, 1, 14) to true,
            LocalDate(2023, 1, 15) to true,
            LocalDate(2023, 1, 16) to true,
            LocalDate(2023, 1, 17) to true,
            LocalDate(2023, 1, 18) to true,
            LocalDate(2023, 1, 19) to true,
            LocalDate(2023, 1, 20) to true,

            LocalDate(2023, 1, 21) to false,
        )

        val dateToInclusionStatusMap = mutableMapOf<LocalDate, Boolean>()
        for (date in startDate..endDate) {
            dateToInclusionStatusMap[date] = streaks.checkIfContainDate(date)
        }

        assertThat(dateToInclusionStatusMap).containsExactlyEntriesIn(expectedMap)
    }
}