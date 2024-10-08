package com.rendox.routinetracker.routinedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.domain.completiondata.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.domain.di.DeleteHabitUseCase
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.domain.streak.GetStreaksInPeriodUseCase
import com.rendox.routinetracker.core.domain.streak.stats.GetCurrentStreakUseCase
import com.rendox.routinetracker.core.domain.streak.stats.GetLongestStreakUseCase
import com.rendox.routinetracker.core.logic.contains
import com.rendox.routinetracker.core.logic.getDurationInDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.helpers.UiEvent
import java.time.YearMonth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn

class RoutineDetailsScreenViewModel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val routineId: Long,
    private val getHabit: GetHabitUseCase,
    private val getHabitCompletionData: GetHabitCompletionDataUseCase,
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val deleteHabit: DeleteHabitUseCase,
    private val getCurrentStreak: GetCurrentStreakUseCase,
    private val getLongestStreak: GetLongestStreakUseCase,
    private val getStreaksInPeriod: GetStreaksInPeriodUseCase,
) : ViewModel() {
    private val _habitFlow: MutableStateFlow<Habit?> = MutableStateFlow(null)
    val habitFlow: StateFlow<Habit?> = _habitFlow.asStateFlow()

    private val todayFlow = MutableStateFlow(today)

    private val _calendarDatesFlow = MutableStateFlow(emptyMap<LocalDate, CalendarDateData>())
    val calendarDatesFlow = _calendarDatesFlow.asStateFlow()

    private val initialMonth = YearMonth.from(todayFlow.value.toJavaLocalDate())
    private val currentMonthFlow: MutableStateFlow<YearMonth> = MutableStateFlow(initialMonth)

    private val _currentStreakDurationInDays = MutableStateFlow(0)
    val currentStreakDurationInDays = _currentStreakDurationInDays.asStateFlow()

    private val _longestStreakDurationInDays = MutableStateFlow(0)
    val longestStreakDurationInDays = _longestStreakDurationInDays.asStateFlow()

    private val _completionAttemptBlockedEvent: MutableStateFlow<UiEvent<IllegalDateEditAttemptException>?> =
        MutableStateFlow(null)
    val completionAttemptBlockedEvent = _completionAttemptBlockedEvent.asStateFlow()

    private val _navigateBackEvent: MutableStateFlow<UiEvent<Any>?> = MutableStateFlow(null)
    val navigateBackEvent = _navigateBackEvent.asStateFlow()

    init {
        viewModelScope.launch {
            val habit = getHabit(routineId)
            _habitFlow.update { habit }
            updateStreaks(habit)
            updateMonthsWithMargin(habit)
        }
    }

    private suspend fun updateMonthsWithMargin(
        habit: Habit,
        forceUpdate: Boolean = false,
    ) {
        // delete all other months because the data may be outdated
        if (forceUpdate) {
            val start = currentMonthFlow.value.minusMonths(NUM_OF_MONTHS_TO_LOAD_AHEAD.toLong())
                .atStartOfMonth().toKotlinLocalDate()
            val end = currentMonthFlow.value.plusMonths(NUM_OF_MONTHS_TO_LOAD_AHEAD.toLong())
                .atEndOfMonth().toKotlinLocalDate()
            _calendarDatesFlow.update { calendarDates ->
                calendarDates.filterKeys { it in start..end }
            }
        }
        updateMonth(habit, currentMonthFlow.value, forceUpdate)
        for (i in 1..NUM_OF_MONTHS_TO_LOAD_AHEAD) {
            updateMonth(habit, currentMonthFlow.value.plusMonths(i.toLong()), forceUpdate)
            updateMonth(habit, currentMonthFlow.value.minusMonths(i.toLong()), forceUpdate)
        }
    }

    /**
     * @param forceUpdate update the data even if it's already loaded
     */
    private suspend fun updateMonth(
        habit: Habit,
        monthToUpdate: YearMonth,
        forceUpdate: Boolean = false,
    ) {
        if (!forceUpdate) {
            val dataForMonthIsAlreadyLoaded = _calendarDatesFlow.value.keys.any {
                it.toJavaLocalDate().yearMonth == monthToUpdate
            }
            if (dataForMonthIsAlreadyLoaded) return
        }

        val monthStart = monthToUpdate.atStartOfMonth().toKotlinLocalDate()
        val monthEnd = monthToUpdate.atEndOfMonth().toKotlinLocalDate()
        val datesCompletionData = getHabitCompletionData(
            habitId = routineId,
            validationDates = monthStart..monthEnd,
            today = todayFlow.value,
        )
        val streaks = getStreaksInPeriod(
            habit = habit,
            period = monthStart..monthEnd,
            today = todayFlow.value,
        )
        val calendarDateData = datesCompletionData.mapValues { (date, completionData) ->
            CalendarDateData(
                status = completionData.habitStatus,
                includedInStreak = streaks.any { it.contains(date) },
                numOfTimesCompleted = completionData.numOfTimesCompleted,
                isPastDate = date <= todayFlow.value,
            )
        }
        _calendarDatesFlow.update {
            it.toMutableMap().apply { putAll(calendarDateData) }
        }
    }

    fun onScrolledToNewMonth(newMonth: YearMonth) {
        currentMonthFlow.update { newMonth }
        if (newMonth != initialMonth) {
            viewModelScope.launch {
                updateMonthsWithMargin(_habitFlow.value!!)
            }
        }
    }

    fun onHabitComplete(completionRecord: Habit.CompletionRecord) = viewModelScope.launch {
        try {
            insertHabitCompletion(
                habitId = routineId,
                completionRecord = completionRecord,
                today = todayFlow.value,
            )
        } catch (exception: IllegalDateEditAttemptException) {
            _completionAttemptBlockedEvent.update {
                object : UiEvent<IllegalDateEditAttemptException> {
                    override val data: IllegalDateEditAttemptException = exception
                    override fun onConsumed() {
                        _completionAttemptBlockedEvent.update { null }
                    }
                }
            }
        }

        coroutineScope {
            launch { updateStreaks(_habitFlow.value!!) }
            launch { updateMonthsWithMargin(habit = _habitFlow.value!!, forceUpdate = true) }
        }
    }

    fun onDeleteHabit() = viewModelScope.launch {
        _habitFlow.value?.let { habit ->
            deleteHabit(habit.id!!)
        }
        _navigateBackEvent.update {
            object : UiEvent<Any> {
                override val data: Any = Unit
                override fun onConsumed() {
                    _navigateBackEvent.update { null }
                }
            }
        }
    }

    private suspend fun updateStreaks(habit: Habit) {
        _currentStreakDurationInDays.update {
            getCurrentStreak(habit, todayFlow.value)?.getDurationInDays() ?: 0
        }
        _longestStreakDurationInDays.update {
            getLongestStreak(habit, todayFlow.value)?.getDurationInDays() ?: 0
        }
    }

    companion object {
        /**
         * The number of months that should be loaded ahead or behind the current month (depending
         * on the user's scroll direction). It's done for the user to see the pre-loaded data
         * when they scroll to the next month.
         */
        const val NUM_OF_MONTHS_TO_LOAD_AHEAD = 3
    }
}

data class CalendarDateData(
    val status: HabitStatus,
    val includedInStreak: Boolean,
    val numOfTimesCompleted: Float,
    val isPastDate: Boolean,
)