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

    private val calendarTl = object : ThreadLocal<Calendar>() {
        override fun initialValue(): Calendar = Calendar.getInstance()
    }

    private val secondaryCalendarTl = object : ThreadLocal<Calendar>() {
        override fun initialValue(): Calendar = Calendar.getInstance()
    }

    private val dateTl = object : ThreadLocal<Date>() {
        override fun initialValue(): Date = Date()
    }

    private val sdfCacheTl = object : ThreadLocal<HashMap<String, SimpleDateFormat>>() {
        override fun initialValue(): HashMap<String, SimpleDateFormat> = HashMap(12)
    }

    private fun formatWithPattern(pattern: String, millis: Long): String {
        val map = sdfCacheTl.get()!!
        var sdf = map[pattern]
        if (sdf == null) {
            sdf = SimpleDateFormat(pattern, locale)
            map[pattern] = sdf
        }
        val d = dateTl.get()!!
        d.time = millis
        return sdf.format(d)
    }

    fun currentYearMonthKey(nowMillis: Long = System.currentTimeMillis()): String {
        val cal = calendarTl.get()!!
        cal.timeInMillis = nowMillis
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(locale, "%04d-%02d", year, month)
    }

    fun shiftYearMonthKey(yearMonthKey: String, deltaMonths: Int): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, deltaMonths)
        return String.format(locale, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun parseYearMonth(yearMonthKey: String): Pair<Int, Int> {
        val dashIdx = yearMonthKey.indexOf('-')
        if (dashIdx > 0) {
            val year = yearMonthKey.substring(0, dashIdx).toIntOrNull()
            val month = yearMonthKey.substring(dashIdx + 1).toIntOrNull()?.coerceIn(1, 12)
            if (year != null && month != null) return year to month
        }
        val now = calendarTl.get()!!
        now.timeInMillis = System.currentTimeMillis()
        val year = yearMonthKey.substringBefore("-").toIntOrNull() ?: now.get(Calendar.YEAR)
        val month = yearMonthKey.substringAfter("-", "").toIntOrNull()?.coerceIn(1, 12) ?: (now.get(Calendar.MONTH) + 1)
        return year to month
    }

    fun formatMonthYear(yearMonthKey: String, shortMonth: Boolean = false): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val pattern = if (shortMonth) "MMM yyyy" else "MMMM yyyy"
        return formatWithPattern(pattern, cal.timeInMillis)
    }

    fun formatShortMonth(yearMonthKey: String): String {
        val (year, month) = parseYearMonth(yearMonthKey)
        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return formatWithPattern("MMM", cal.timeInMillis)
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

        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, safeStartDay.coerceAtMost(maxDay))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val maxNextDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, safeStartDay.coerceAtMost(maxNextDay))
        cal.add(Calendar.MILLISECOND, -1)
        val endMillis = cal.timeInMillis

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
        val cal = calendarTl.get()!!
        cal.timeInMillis = nowMillis
        cal.firstDayOfWeek = firstDayOfWeek
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
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
        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, quarterStartMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis
        cal.add(Calendar.MONTH, 3)
        cal.add(Calendar.MILLISECOND, -1)
        return startMillis to cal.timeInMillis
    }

    fun getYearRange(yearMonthKey: String): Pair<Long, Long> {
        val (year, _) = parseYearMonth(yearMonthKey)
        val cal = calendarTl.get()!!
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis
        cal.add(Calendar.YEAR, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return startMillis to cal.timeInMillis
    }

    fun formatDate(millis: Long, option: DateFormatOption = DateFormatOption.DD_MM_YYYY): String {
        return formatWithPattern(option.pattern, millis)
    }

    fun formatTime(millis: Long): String {
        return formatWithPattern("hh:mm a", millis)
    }

    fun formatMonthDay(millis: Long): String {
        return formatWithPattern("MMM d", millis)
    }

    fun formatRelativeDayHeader(millis: Long, nowMillis: Long = System.currentTimeMillis()): Pair<String, String> {
        val targetDay = startOfDay(millis)
        val today = startOfDay(nowMillis)
        val yesterday = today - 86_400_000L
        val dateText = formatWithPattern("d MMM yyyy", millis)
        val prefix = when {
            targetDay == today -> "Today"
            targetDay == yesterday -> "Yesterday"
            else -> formatWithPattern("EEEE", millis)
        }
        return prefix to dateText
    }

    fun formatShortTimestamp(millis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val targetDay = startOfDay(millis)
        val today = startOfDay(nowMillis)
        val yesterday = today - 86_400_000L
        return when (targetDay) {
            today -> "Today ${formatTime(millis)}"
            yesterday -> "Yesterday ${formatTime(millis)}"
            else -> formatMonthDay(millis)
        }
    }

    fun startOfDay(millis: Long): Long {
        val cal = calendarTl.get()!!
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        val cal = calendarTl.get()!!
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun formatRangeLabel(startMillis: Long, endMillis: Long): String {
        val startCal = calendarTl.get()!!.apply { timeInMillis = startMillis }
        val endCal = secondaryCalendarTl.get()!!.apply { timeInMillis = endMillis }
        val sameYear = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)
        val sameMonth = sameYear && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
        return when {
            sameMonth -> {
                val month = formatWithPattern("MMM", startMillis)
                val d1 = startCal.get(Calendar.DAY_OF_MONTH)
                val d2 = endCal.get(Calendar.DAY_OF_MONTH)
                val year = endCal.get(Calendar.YEAR)
                "$month $d1 – $d2, $year"
            }
            sameYear -> {
                val f1 = formatWithPattern("MMM d", startMillis)
                val f2 = formatWithPattern("MMM d, yyyy", endMillis)
                "$f1 – $f2"
            }
            else -> {
                val f1 = formatWithPattern("MMM d, yyyy", startMillis)
                val f2 = formatWithPattern("MMM d, yyyy", endMillis)
                "$f1 – $f2"
            }
        }
    }
}
