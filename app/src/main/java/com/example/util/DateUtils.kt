package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * API 24-safe calendar day utility using local epoch days (days since 1970-01-01 in local calendar).
 */
object DateUtils {

    data class MonthCalendar(
        val year: Int,
        val monthZeroBased: Int, // 0 = Jan, 11 = Dec
        val monthName: String,
        val showYear: Boolean,
        val leadingEmptySlots: Int,
        val days: List<DayCell>
    )

    data class DayCell(
        val dayOfMonth: Int,
        val epochDay: Long
    )

    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        return ymdToEpochDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun timestampToEpochDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return ymdToEpochDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun ymdToEpochDay(year: Int, monthZeroBased: Int, dayOfMonth: Int): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, monthZeroBased, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return utc.timeInMillis / 86_400_000L
    }

    fun epochDayToCalendar(epochDay: Long): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = epochDay * 86_400_000L
        }
    }

    /** Formats e.g. "21 Nov" (matches Buckwheat screenshots). */
    fun formatDayMonth(epochDay: Long): String {
        val cal = epochDayToCalendar(epochDay)
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(cal.time)
    }

    /** Formats e.g. "Sat, 21 Nov" for the minimal swipeable date pill. */
    fun formatWeekdayDate(epochDay: Long): String {
        val cal = epochDayToCalendar(epochDay)
        val sdf = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(cal.time)
    }

    /** Returns the last day of the current calendar month as an epochDay. */
    fun endOfCurrentMonthEpochDay(): Long {
        val cal = Calendar.getInstance()
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return ymdToEpochDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            lastDay
        )
    }

    /** Returns e.g. "November Budget" for quick monthly budget defaults. */
    fun currentMonthBudgetName(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        return "${sdf.format(cal.time)} Budget"
    }

    /** Formats e.g. "2026-11-21" for CSV export. */
    fun formatIsoDate(epochDay: Long): String {
        val cal = epochDayToCalendar(epochDay)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(cal.time)
    }

    /** Formats local timestamp e.g. "10:07 AM" or "21:45". */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /** Formats full date-time for CSV export. */
    fun formatIsoDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date(timestamp))
    }

    /** Inclusive days count between two epochDays (e.g. 21 Nov to 30 Nov = 10 days). */
    fun inclusiveDaysBetween(startEpochDay: Long, finishEpochDay: Long): Int {
        return ((finishEpochDay - startEpochDay) + 1L).toInt().coerceAtLeast(0)
    }

    /** Builds a list of calendar months (optionally including prior months) for the vertical date picker. */
    fun buildUpcomingMonths(
        monthsCount: Int = 12,
        monthsBefore: Int = 0,
        startWeekOnMonday: Boolean = false
    ): List<MonthCalendar> {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val startMonth = now.get(Calendar.MONTH)
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())

        val totalMonths = monthsBefore + monthsCount
        val result = ArrayList<MonthCalendar>(totalMonths)
        for (offset in -monthsBefore until monthsCount) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, startMonth + offset)
            }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun .. 7 = Sat

            val leadingEmpty = if (startWeekOnMonday) {
                if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
            } else {
                firstDayOfWeek - Calendar.SUNDAY
            }

            val dayCells = (1..daysInMonth).map { day ->
                DayCell(dayOfMonth = day, epochDay = ymdToEpochDay(year, month, day))
            }

            result.add(
                MonthCalendar(
                    year = year,
                    monthZeroBased = month,
                    monthName = monthFormat.format(cal.time),
                    showYear = year != currentYear,
                    leadingEmptySlots = leadingEmpty,
                    days = dayCells
                )
            )
        }
        return result
    }
}
