package com.rendox.routinetracker.routine_details.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.components.WrapTextContent
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.round
import kotlin.reflect.KClass

@Composable
fun RoutineProperties(
    modifier: Modifier = Modifier,
    description: String?,
    progress: Float?,
    scheduleType: KClass<out Schedule>,
    numOfDueDaysPerPeriod: Int,
    numOfDaysInPeriodForAlternateDaysSchedule: Int?,
    sessionDurationMinutes: Int?,
    completionTime: LocalTime?,
    reminderEnabled: Boolean,
//    numericalValueHabitUnit: Habit.NumericalValueRoutineUnit?,
) {
    Column(modifier = modifier) {
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        progress?.let {
            Row(
                Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = "${round(it * 100).toInt()} %",
                    style = MaterialTheme.typography.labelSmall,
                )


                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .fillMaxWidth(),
                    progress = { it },
                    strokeCap = StrokeCap.Round,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            val frequencyString = deriveRoutineFrequency(
                scheduleType, numOfDueDaysPerPeriod, numOfDaysInPeriodForAlternateDaysSchedule
            )
            val sessionDurationString =
                sessionDurationMinutes?.let { deriveRoutineSessionDuration(it) }

            frequencyString?.let {
                RoutineDetailLabel(
                    modifier = Modifier.weight(weight = 3.7f, fill = false),
                    icon = painterResource(
                        id = com.rendox.routinetracker.feature.routine_details.R.drawable.baseline_calendar_month_24
                    ),
                    text = it,
                )
            }

            sessionDurationString?.let {
                RoutineDetailLabel(
                    modifier = Modifier.weight(weight = 3.3f, fill = false),
                    icon = painterResource(
                        id = com.rendox.routinetracker.feature.routine_details.R.drawable.baseline_access_time_filled_24
                    ),
                    text = it,
                )
            }

            val routineTimeIcon: Painter
            val routineTimeIconContentDescription: String

            if (reminderEnabled) {
                routineTimeIcon = painterResource(
                    id = com.rendox.routinetracker.feature.routine_details.R.drawable.baseline_notifications_enabled_24
                )
                routineTimeIconContentDescription =
                    stringResource(id = com.rendox.routinetracker.feature.routine_details.R.string.detail_label_notifications_enabled_icon_description)
            } else {
                routineTimeIcon =
                    painterResource(
                        id = com.rendox.routinetracker.feature.routine_details.R.drawable.baseline_notifications_disabled_24
                    )
                routineTimeIconContentDescription =
                    stringResource(
                        id = com.rendox.routinetracker.feature.routine_details.R.string.detail_label_notifications_disabled_icon_description
                    )
            }

            val locale = LocalLocale.current
            val timeFormatter =
                remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
            val completionTimeString: String? =
                completionTime?.toJavaLocalTime()?.format(timeFormatter)

            completionTimeString?.let {
                RoutineDetailLabel(
                    modifier = Modifier.weight(weight = 3f, fill = false),
                    icon = routineTimeIcon,
                    text = it,
                    iconContentDescription = routineTimeIconContentDescription,
                )
            }
        }
    }
}

@Composable
private fun deriveRoutineFrequency(
    scheduleType: KClass<out Schedule>,
    numOfDueDaysPerPeriod: Int,
    numOfDaysInPeriodForAlternateDaysSchedule: Int?,
): String? {
    return when (scheduleType) {
        Schedule.EveryDaySchedule::class ->
            stringResource(id = R.string.frequency_every_day)

        Schedule.WeeklySchedule::class -> {
            val numOfDays = pluralStringResource(
                id = R.plurals.num_of_days,
                count = numOfDueDaysPerPeriod,
                numOfDueDaysPerPeriod,
            )
            val perWeek = stringResource(
                id = R.string.frequency_weekly
            )
            "$numOfDays $perWeek"
        }

        Schedule.AlternateDaysSchedule::class -> {
            val numOfActivityDaysString = pluralStringResource(
                id = R.plurals.num_of_days,
                count = numOfDueDaysPerPeriod,
                numOfDueDaysPerPeriod,
            )
            val activity = stringResource(id = R.string.frequency_activity)
            val numOfRestDays =
                numOfDaysInPeriodForAlternateDaysSchedule!! - numOfDueDaysPerPeriod
            val numOfRestDaysString = pluralStringResource(
                id = R.plurals.num_of_days,
                count = numOfRestDays,
                numOfRestDays,
            )
            val rest = stringResource(id = R.string.frequency_rest)

            "$numOfActivityDaysString $activity\n$numOfRestDaysString $rest"
        }

        Schedule.MonthlySchedule::class -> {
            val numOfDays = pluralStringResource(
                id = R.plurals.num_of_days,
                count = numOfDueDaysPerPeriod,
                numOfDueDaysPerPeriod,
            )
            val perMonth = stringResource(id = R.string.frequency_monthly)
            "$numOfDays $perMonth"
        }

        Schedule.AnnualSchedule::class -> {
            val numOfDays = pluralStringResource(
                id = R.plurals.num_of_days,
                count = numOfDueDaysPerPeriod,
                numOfDueDaysPerPeriod,
            )
            val perYear = stringResource(id = R.string.frequency_annually)
            "$numOfDays $perYear"
        }

        Schedule.CustomDateSchedule::class -> null
        else -> throw IllegalArgumentException()
    }
}

@Composable
private fun deriveRoutineSessionDuration(sessionDurationMinutes: Int): String {
    val hours = sessionDurationMinutes.div(60f).toInt()
    val minutes = sessionDurationMinutes.rem(60)

    val hoursString = if (hours == 0) "" else stringResource(
        id = com.rendox.routinetracker.feature.routine_details.R.string.detail_label_session_duration_hours, hours
    )
    val minutesString = if (minutes == 0) "" else stringResource(
        id = com.rendox.routinetracker.feature.routine_details.R.string.detail_label_session_duration_minutes, minutes
    )
    return "$hoursString $minutesString"
}

@Composable
private fun deriveAmountOfWorkPerSession(
    numOfUnitsPerSession: Int,
    unitsOfMeasure: String,
    sessionUnit: DateTimeUnit,
): String {
    val perPeriod = when (sessionUnit) {
        DateTimeUnit.DAY -> stringResource(id = R.string.frequency_daily)
        DateTimeUnit.WEEK -> stringResource(id = R.string.frequency_weekly)
        DateTimeUnit.MONTH -> stringResource(id = R.string.frequency_monthly)
        DateTimeUnit.YEAR -> stringResource(id = R.string.frequency_annually)
        else -> throw IllegalArgumentException()
    }
    return "$numOfUnitsPerSession $unitsOfMeasure $perPeriod"
}

@Composable
private fun RoutineDetailLabel(
    modifier: Modifier = Modifier,
    icon: Painter,
    iconContentDescription: String? = null,
    text: String,
) {
    Row(
        modifier = modifier
            .padding(top = 16.dp)
            .padding(end = 16.dp),
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Top)
                .size(24.dp)
                .padding(end = 4.dp),
            painter = icon,
            contentDescription = iconContentDescription,
        )

        WrapTextContent(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            lineHeight = 13.sp,
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun RoutinePropertiesPreview() {
    Surface {
        RoutineProperties(
            description = "Stay fit and healthy. Embrace the benefits " +
                    "of exercise and elevate your overall health with this " +
                    "essential daily routine.",
            progress = 0.75f,
            scheduleType = Schedule.AnnualSchedule::class,
            numOfDueDaysPerPeriod = 90,
            numOfDaysInPeriodForAlternateDaysSchedule = null,
            sessionDurationMinutes = 65,
            completionTime = LocalTime(23, 15),
            reminderEnabled = true,
//            numericalValueHabitUnit = Habit.NumericalValueRoutineUnit(
//                numOfUnitsPerSession = 7,
//                unitsOfMeasure = "books",
//                DateTimeUnit.YEAR,
//            ),
        )
    }
}