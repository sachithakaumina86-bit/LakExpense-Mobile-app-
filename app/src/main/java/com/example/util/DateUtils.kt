package com.example.util

import com.example.data.model.DateFormatOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CycleRange(
    val yearMonthKey: String,
    val startMillis: Long,
    val endMillis: Long,
    val totalDays: Int,
    val daysElapsed: Int,
    val daysRemaining: Int
)

object DateUtils {
    private val locale = Locale.US

    fun currentYearMonthKey(nowMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(locale, "%04d-%02d", year, month)
    }

    fun shiftYearMonthKey(yearMonthKey: String, deltaMonths: Int): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        return String.format(locale, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun parseYearMonth(yearMonthKey: String): Pair<Int, Int> {
        val parts = yearMonthKey.split("-")
        val now = Calendar.getInstance()
        val year = parts.getOrNull(0)?.toIntOrNull() ?: now.get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 12) ?: (now.get(Calendar.MONTH) + 1)
        return year to month
    }

    fun formatMonthYear(yearMonthKey: String, shortMonth: Boolean = false): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val pattern = if (shortMonth) "MMM yyyy" else "MMMM yyyy"
        return SimpleDateFormat(pattern, locale).format(cal.time)
    }

    fun formatShortMonth(yearMonthKey: String): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM", locale).format(cal.time)
    }

    /**
     * Computes exact [startMillis, endMillis] for the given yearMonthKey anchored on cycleStartDay (1..28).
     */
    fun getCycleRange(
        yearMonthKey: String,
        cycleStartDay: Int = 1,
        nowMillis: Long = System.currentTimeMillis()
    ): CycleRange {
        val (year, month) = parseYearMonth(yearMonthKey)
        val safeStartDay = cycleStartDay.coerceIn(1, 28)

        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, safeStartDay.coerceAtMost(maxDay))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            val maxNextDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, safeStartDay.coerceAtMost(maxNextDay))
            add(Calendar.MILLISECOND, -1)
        }

        val startMillis = startCal.timeInMillis
        val endMillis = nextCal.timeInMillis
        val dayMs = 86_400_000L
        val totalDays = (((endMillis - startMillis) / dayMs) + 1).toInt().coerceAtLeast(1)

        val clampedNow = nowMillis.coerceIn(startMillis, endMillis)
        val daysElapsed = if (nowMillis < startMillis) {
            0
        } else {
            (((clampedNow - startMillis) / dayMs) + 1).toInt().coerceIn(1, totalDays)
        }
        val daysRemaining = if (nowMillis > endMillis) {
            0
        } else if (nowMillis < startMillis) {
            totalDays
        } else {
            (totalDays - daysElapsed + 1).coerceAtLeast(1)
        }

        return CycleRange(
            yearMonthKey = yearMonthKey,
            startMillis = startMillis,
            endMillis = endMillis,
            totalDays = totalDays,
            daysElapsed = daysElapsed,
            daysRemaining = daysRemaining
        )
    }

    fun getWeekRange(firstDayOfWeek: Int = Calendar.MONDAY, nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            this.firstDayOfWeek = firstDayOfWeek
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (cal.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        cal.add(Calendar.MILLISECOND, -1)
        return start to cal.timeInMillis
    }

    fun getQuarterRange(yearMonthKey: String): Pair<Long, Long> {
        val (year, month) = parseYearMonth(yearMonthKey)
        val quarterStartMonth = ((month - 1) / 3) * 3
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, quarterStartMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 3)
            add(Calendar.MILLISECOND, -1)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun getYearRange(yearMonthKey: String): Pair<Long, Long> {
        val (year, _) = parseYearMonth(yearMonthKey)
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.YEAR, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun formatDate(millis: Long, option: DateFormatOption = DateFormatOption.DD_MM_YYYY): String {
        return SimpleDateFormat(option.pattern, locale).format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        return SimpleDateFormat("hh:mm a", locale).format(Date(millis))
    }

    fun formatMonthDay(millis: Long): String {
        return SimpleDateFormat("MMM d", locale).format(Date(millis))
    }

    fun formatRelativeDayHeader(millis: Long, nowMillis: Long = System.currentTimeMillis()): Pair<String, String> {
        val targetDay = startOfDay(millis)
        val today = startOfDay(nowMillis)
        val yesterday = today - 86_400_000L
        val dateText = SimpleDateFormat("d MMM yyyy", locale).format(Date(millis))
        val prefix = when {
            targetDay == today -> "Today"
            targetDay == yesterday -> "Yesterday"
            else -> SimpleDateFormat("EEEE", locale).format(Date(millis))
        }
        return prefix to dateText
    }

    fun formatShortTimestamp(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val targetDay = startOfDay(millis)
        val today = startOfDay(nowMillis)
        val yesterday = today - 86_400_000L
        val time = formatTime(millis)
        return when (targetDay) {
            today -> "Today $time"
            yesterday -> "Yesterday $time"
            else -> SimpleDateFormat("MMM d", locale).format(Date(millis))
        }
    }

    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun formatRangeLabel(startMillis: Long, endMillis: Long): String {
        val startCal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }
        val sameYear = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
        val sameMonth = sameYear && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
        return when {
            sameMonth -> {
                val month = SimpleDateFormat("MMM", locale).format(Date(startMillis))
                val d1 = startCal.get(Calendar.DAY_OF_MONTH)
                val d2 = endCal.get(Calendar.DAY_OF_MONTH)
                val year = endCal.get(Calendar.YEAR)
                "$month $d1 – $d2, $year"
            }
            sameYear -> {
                val f1 = SimpleDateFormat("MMM d", locale).format(Date(startMillis))
                val f2 = SimpleDateFormat("MMM d, yyyy", locale).format(Date(endMillis))
                "$f1 – $f2"
            }
            else -> {
                val f = SimpleDateFormat("MMM d, yyyy", locale)
                "${f.format(Date(startMillis))} – ${f.format(Date(endMillis))}"
            }
        }
    }
}
