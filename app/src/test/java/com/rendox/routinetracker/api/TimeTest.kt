package com.rendox.routinetracker.api

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.atEndOfMonth
import com.rendox.routinetracker.logic.WeekDayMonthRelatedPattern
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Test

class WeekDayRelativeToMonthTest {

    @Test
    fun `derive weekDayRelativeToMonth from 3rd Friday of August, 2023`() {
        val experimentalDate = LocalDate(2023, 8, 18)
        val weekDayRelativeToMonth = with(WeekDayRelativeToMonth) {
            deriveExplicitFrom(getNumber(experimentalDate))
        }
        assertThat(weekDayRelativeToMonth).isEqualTo(WeekDayRelativeToMonth.Third)
    }

    @Test
    fun `derive weekDayRelativeToMonth from 5th Wednesday of August, 2023`() {
        val experimentalDate = LocalDate(2023, 8, 30)
        val weekDayRelativeToMonth = with(WeekDayRelativeToMonth) {
            deriveExplicitFrom(getNumber(experimentalDate))
        }
        assertThat(weekDayRelativeToMonth).isEqualTo(WeekDayRelativeToMonth.Fifth)
    }

    @Test
    fun `derive weekDayRelativeToMonth from 4th Monday of August, 2023`() {
        val experimentalDate = LocalDate(2023, 8, 28)
        val weekDayRelativeToMonth = with(WeekDayRelativeToMonth) {
            deriveExplicitFrom(getNumber(experimentalDate))
        }
        assertThat(weekDayRelativeToMonth).isEqualTo(WeekDayRelativeToMonth.Forth)
    }
}

class WeekDayMonthRelatedPatternTest {

    @Test
    fun `forth Tuesday pattern matches forth Tuesday of February, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.TUESDAY.value, WeekDayRelativeToMonth.Forth)
        val matches = pattern.matches(LocalDate(2023, 2, 28))
        assertThat(matches).isTrue()
    }

    @Test
    fun `last Tuesday pattern matches forth Tuesday of February, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.TUESDAY.value, WeekDayRelativeToMonth.Last)
        val matches = pattern.matches(LocalDate(2023, 2, 28))
        assertThat(matches).isTrue()
    }

    @Test
    fun `last Thursday pattern doesn't match forth Thursday of August, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.THURSDAY.value, WeekDayRelativeToMonth.Last)
        val matches = pattern.matches(LocalDate(2023, 8, 24))
        assertThat(matches).isFalse()
    }

    @Test
    fun `last Wednesday pattern matches fifth Wednesday of August, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.WEDNESDAY.value, WeekDayRelativeToMonth.Last)
        val matches = pattern.matches(LocalDate(2023, 8, 30))
        assertThat(matches).isTrue()
    }

    @Test
    fun `first Friday pattern doesn't match second Friday of August, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.FRIDAY.value, WeekDayRelativeToMonth.First)
        val matches = pattern.matches(LocalDate(2023, 8, 11))
        assertThat(matches).isFalse()
    }

    @Test
    fun `second Saturday pattern doesn't match second Sunday of August, 2023`() {
        val pattern =
            WeekDayMonthRelatedPattern(DayOfWeek.SATURDAY.value, WeekDayRelativeToMonth.Second)
        val matches = pattern.matches(LocalDate(2023, 8, 13))
        assertThat(matches).isFalse()
    }
}

class TimeTest {

    @Test
    fun `calculate last day of leap year's February`() {
        val leapYear = 2020
        val date = LocalDate(leapYear, Month.FEBRUARY, 1)
        val lastDayOfMonth = date.atEndOfMonth
        assertThat(lastDayOfMonth).isEqualTo(LocalDate(leapYear, java.time.Month.FEBRUARY, 29))
    }

    @Test
    fun `calculate last day of 30-day month`() {
        val year = 2023
        val date = LocalDate(year, Month.SEPTEMBER, 1)
        val lastDayOfMonth = date.atEndOfMonth
        assertThat(lastDayOfMonth).isEqualTo(LocalDate(year, java.time.Month.SEPTEMBER, 30))
    }

    @Test
    fun `calculate the number of days in month`() {
        val date = LocalDate(2023, Month.SEPTEMBER, 30)
        val daysInMonth = date.atEndOfMonth.dayOfMonth
        assertThat(daysInMonth).isEqualTo(30)
    }
}