package com.rendox.routinetracker.logic

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.rangeTo
import com.rendox.performancetracker.domain.model.HistoricalStatus
import com.rendox.performancetracker.domain.model.PlanningStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import org.junit.Before
import org.junit.Test

class RoutineStatusTest {
    private lateinit var routine: Routine
    private val past = LocalDate(2023, Month.JULY, 1)
    private val future = LocalDate(2023, Month.OCTOBER, 28)

    private val week1 = listOf(
        // 2023-08-28 / 0
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-08-29 / 1
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-08-30 / 2
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
        // 2023-08-31 / 3
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
        // 2023-09-01 / 4
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-02 / 5
        com.rendox.performancetracker.domain.model.HistoricalStatus.NotCompleted,
        // 2023-09-03 / 6
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
    )
    private val week2 = listOf(
        // 2023-09-04 / 7
        com.rendox.performancetracker.domain.model.HistoricalStatus.NotCompleted,
        // 2023-09-05 / 8
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-06 / 9
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-07 / 10
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-08 / 11
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-09 / 12
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
        // 2023-09-10 / 13
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
    )
    private val week3 = listOf(
        // 2023-09-11 / 14
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-12 / 15
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-13 / 16
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-14 / 17
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-15 / 18
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-16 / 19
        com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
        // 2023-09-17 / 20
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
    )
    private val week4 = listOf(
        // 2023-09-18 / 21
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
        // 2023-09-19 / 22
        com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
    )

    @Before
    fun setUp() {
        val startDate = LocalDate(2023, Month.AUGUST, 28)
        routine = YesNoRoutine(
            title = "Do sports",
            description = "Move every day",
            startDate = startDate,
            schedule = WeeklySchedule(List(5) { it }),
        )
        val history = mutableListOf<com.rendox.performancetracker.domain.model.HistoricalStatus>()
        history.addAll(week1)
        history.addAll(week2)
        history.addAll(week3)
        history.addAll(week4)
        routine.completionHistory = history
    }

    @Test
    fun nonExistentTest() {
        assertThat(routine.computeStatus(past)).isEqualTo(com.rendox.performancetracker.domain.model.PlanningStatus.Unknown)
    }

    @Test
    fun historyTest() {
        val specifiedDay = LocalDate(2023, Month.AUGUST, 29)
        assertThat(routine.computeStatus(specifiedDay)).isEqualTo(com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted)
    }
    @Test
    fun `random Sunday, week is different from current, must be not due`() {
        val date = LocalDate(2023, Month.DECEMBER, 31)
        assertThat(routine.computeStatus(date)).isEqualTo(com.rendox.performancetracker.domain.model.PlanningStatus.NotDue)
    }

    // TODO fails because the weeks do not start from Monday !
    @Test
    fun `random Friday, week is different from current, must be due`() {
        val date = LocalDate(2023, Month.OCTOBER, 13)
        assertThat(routine.computeStatus(date)).isEqualTo(com.rendox.performancetracker.domain.model.PlanningStatus.Planned)
    }

    @Test
    fun `Wednesday of the 4th week, must be due`() {
        val date = LocalDate(2023, Month.SEPTEMBER, 23)
        assertThat(routine.computeStatus(date)).isEqualTo(com.rendox.performancetracker.domain.model.PlanningStatus.Planned)
    }

    @Test
    fun general() {
        val start = routine.startDate
        println(start)
        println(routine.completionHistory.size)
        val end = start.plus(DatePeriod(days = routine.completionHistory.size - 1))
        println(end)
        println()
        var weekCounter = 1
        for ((i, date) in (start..end).withIndex()) {
            if ((i % 7) == 0){
                println()
                println("// week #$weekCounter")
                weekCounter++
            }
            println("// $date / $i")
            println("HistoricalStatus.${routine.computeStatus(date)},")
        }
    }

//    @Test
//    fun periodInHistoryIndices() {
//        val period = routine.periodInHistoryIndices(routine.startDate.plus(DatePeriod(days = 16)))
//        println("indices = $period")
//        println()
//        for (i in period) {
//            println("    $i / ${routine.startDate.plus(DatePeriod(days = i))} — ${routine.completionHistory[i]}")
//        }
//    }
}

class RoutineHistoryTest {
    private lateinit var routine: Routine

    @Before
    fun setUp() {
        val startDate = LocalDate(2023, Month.AUGUST, 1)
        routine = YesNoRoutine(
            title = "Do sports",
            description = "Move every day",
            startDate = startDate,
            schedule = WeeklySchedule(List(5) { it }),
        )
        val completionHistory = listOf(
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.PlanningStatus.Planned,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.PlanningStatus.Planned,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
            com.rendox.performancetracker.domain.model.PlanningStatus.NotDue,
        )
    }
}

class RoutineStatusIntegrationTest {

    val routine = YesNoRoutine(
        title = "",
        description = "",
        startDate = LocalDate(2023, Month.SEPTEMBER, 1),
        schedule = WeeklySchedule(
            listOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value, DayOfWeek.THURSDAY.value)
        )
    )

    @Test
    fun add_entry_test() {
        routine.completionHistory = listOf(
            com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted,
            com.rendox.performancetracker.domain.model.HistoricalStatus.Skipped,
        )
        routine.addEntryToHistory(
            date = LocalDate(2023, Month.SEPTEMBER, 10),
            status = com.rendox.performancetracker.domain.model.HistoricalStatus.FullyCompleted
        )
        routine.completionHistory.forEachIndexed {index, status ->
            println("$index / ${routine.startDate.plus(DatePeriod(days = index))} — $status")
        }
    }
}