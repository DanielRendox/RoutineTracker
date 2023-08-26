package com.rendox.routinetracker.logic

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.api.WeekDayRelativeToMonth
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Test

class EveryDayScheduleTest {

    @Test
    fun `every day schedule, due today`() {
        val validationDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val due = EveryDaySchedule.isDue(validationDate)
        assertThat(due).isEqualTo(true)
    }

    @Test
    fun `every day schedule, due in past`() {
        val validationDate = LocalDate(2022, 9, 30)
        val due = EveryDaySchedule.isDue(validationDate)
        assertThat(due).isEqualTo(true)
    }

    @Test
    fun `every day schedule, due in future`() {
        val validationDate = LocalDate(2024, 9, 30)
        val due = EveryDaySchedule.isDue(validationDate)
        assertThat(due).isEqualTo(true)
    }
}

class WeeklyScheduleTest {

    @Test
    fun `weekly schedule by DayOfWeek enum, due on specified days`() {
        val schedule = WeeklySchedule(listOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value))
        assertThat(schedule.isDue(LocalDate(2023, 1, 2))).isTrue() // Monday
        assertThat(schedule.isDue(LocalDate(2023, 8, 23))).isTrue() // Wednesday
        assertThat(schedule.isDue(LocalDate(2023, 8, 29))).isFalse() // Tuesday
    }

    @Test
    fun `weekly schedule by DayOfWeek enum, not due on non-specified days`() {
        val schedule = WeeklySchedule(listOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value))
        assertThat(schedule.isDue(LocalDate(2023, 1, 2))).isTrue() // Monday
        assertThat(schedule.isDue(LocalDate(2023, 8, 23))).isTrue() // Wednesday
        assertThat(schedule.isDue(LocalDate(2023, 8, 29))).isFalse() // Tuesday
    }
}

class MonthlyTraditionalScheduleTest {

    @Test
    fun `monthly traditional schedule, due on specified days`() {
        val schedule = MonthlyTraditionalSchedule(listOf(5, 15, 25))
        assertThat(schedule.isDue(LocalDate(2023, 8, 5))).isTrue()
        assertThat(schedule.isDue(LocalDate(2020, 9, 15))).isTrue()
        assertThat(schedule.isDue(LocalDate(2010, 8, 25))).isTrue()
    }

    @Test
    fun `monthly traditional schedule, not due on non-specified days`() {
        val schedule = MonthlyTraditionalSchedule(listOf(5, 15, 25))
        assertThat(schedule.isDue(LocalDate(2023, 1, 10))).isFalse()
        assertThat(schedule.isDue(LocalDate(2025, 8, 20))).isFalse()
        assertThat(schedule.isDue(LocalDate(2030, 11, 30))).isFalse()
    }

    @Test
    fun `monthly traditional schedule, due on specific day`() {
        val schedule = MonthlyTraditionalSchedule(listOf(10))
        assertThat(schedule.isDue(LocalDate(2023, 8, 10))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 9, 10))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 10, 10))).isTrue()
    }
}

class MonthlyCustomScheduleTest {

    @Test
    fun `monthly custom schedule, due on matching days`() {
        val schedule = MonthlyCustomSchedule(listOf(
            DayOfWeek.MONDAY.value to WeekDayRelativeToMonth.First,
            DayOfWeek.TUESDAY.value to WeekDayRelativeToMonth.Second,
            DayOfWeek.WEDNESDAY.value to WeekDayRelativeToMonth.Third,
            DayOfWeek.SATURDAY.value to WeekDayRelativeToMonth.Forth,
            DayOfWeek.FRIDAY.value to WeekDayRelativeToMonth.Last,
        ))
        assertThat(schedule.isDue(LocalDate(2023, 9, 4))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 10, 2))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 9, 12))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 10, 10))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 9, 20))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 2, 15))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 8, 26))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 10, 28))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 9, 29))).isTrue()
        assertThat(schedule.isDue(LocalDate(2023, 10, 27))).isTrue()
    }

    @Test
    fun `monthly custom schedule, not due on non-matching days`() {
        val schedule = MonthlyCustomSchedule(listOf(
            DayOfWeek.MONDAY.value to WeekDayRelativeToMonth.First,
            DayOfWeek.TUESDAY.value to WeekDayRelativeToMonth.Second,
            DayOfWeek.WEDNESDAY.value to WeekDayRelativeToMonth.Third,
            DayOfWeek.SATURDAY.value to WeekDayRelativeToMonth.Forth,
            DayOfWeek.FRIDAY.value to WeekDayRelativeToMonth.Last,
        ))
        assertThat(schedule.isDue(LocalDate(2023, 9, 11))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 10, 16))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 9, 5))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 10, 31))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 9, 6))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 2, 22))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 8, 5))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 10, 14))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 9, 22))).isFalse()
        assertThat(schedule.isDue(LocalDate(2023, 10, 20))).isFalse()
    }
}
