package com.example.util

import com.example.data.model.CurrencyDisplayOption
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Exact monetary arithmetic using integer cents (Long) and BigDecimal.
 * Avoids IEEE-754 floating-point drift for LKR balances, budgets, and reports.
 */
object MoneyMath {
    private val HUNDRED = BigDecimal(100)
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    fun formatAmount(
        cents: Long,
        currency: CurrencyDisplayOption = CurrencyDisplayOption.RS_PREFIX,
        includeDecimals: Boolean = true,
        showSign: Boolean = false,
        isPositiveOverride: Boolean? = null
    ): String {
        val bd = BigDecimal.valueOf(abs(cents)).divide(HUNDRED, 2, RoundingMode.HALF_UP)
        val pattern = if (includeDecimals) "#,##0.00" else "#,##0"
        val formattedNumber = DecimalFormat(pattern, symbols).format(bd)
        val prefix = currency.symbol
        val sign = when {
            isPositiveOverride == true -> "+"
            isPositiveOverride == false -> "-"
            showSign && cents > 0L -> "+"
            cents < 0L -> "-"
            else -> ""
        }
        return "$sign$prefix $formattedNumber"
    }

    fun formatNumberOnly(cents: Long, includeDecimals: Boolean = true): String {
        val bd = BigDecimal.valueOf(abs(cents)).divide(HUNDRED, 2, RoundingMode.HALF_UP)
        val pattern = if (includeDecimals) "#,##0.00" else "#,##0"
        val sign = if (cents < 0L) "-" else ""
        return sign + DecimalFormat(pattern, symbols).format(bd)
    }

    fun formatCompact(cents: Long): String {
        val absValue = BigDecimal.valueOf(abs(cents)).divide(HUNDRED, 2, RoundingMode.HALF_UP)
        val thousand = BigDecimal(1000)
        val million = BigDecimal(1_000_000)
        val sign = if (cents < 0L) "-" else ""
        return when {
            absValue >= million -> {
                val m = absValue.divide(million, 1, RoundingMode.HALF_UP)
                "${sign}${m.stripTrailingZeros().toPlainString()}M"
            }
            absValue >= thousand -> {
                val k = absValue.divide(thousand, 1, RoundingMode.HALF_UP)
                "${sign}${k.stripTrailingZeros().toPlainString()}k"
            }
            else -> "${sign}${absValue.setScale(0, RoundingMode.HALF_UP).toPlainString()}"
        }
    }

    fun parseToCents(rawInput: String): Long? {
        val cleaned = rawInput
            .replace("Rs.", "", ignoreCase = true)
            .replace("LKR", "", ignoreCase = true)
            .replace(",", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return try {
            val bd = BigDecimal(cleaned)
            if (bd < BigDecimal.ZERO) return null
            bd.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    fun percentage(partCents: Long, totalCents: Long): Int {
        if (totalCents <= 0L || partCents <= 0L) return 0
        return BigDecimal.valueOf(partCents)
            .multiply(HUNDRED)
            .divide(BigDecimal.valueOf(totalCents), 0, RoundingMode.HALF_UP)
            .toInt()
    }

    fun percentageDecimal(partCents: Long, totalCents: Long): Double {
        if (totalCents <= 0L) return 0.0
        return BigDecimal.valueOf(partCents)
            .multiply(HUNDRED)
            .divide(BigDecimal.valueOf(totalCents), 1, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun safeDailyPace(remainingCents: Long, daysRemaining: Int): Long {
        if (remainingCents <= 0L || daysRemaining <= 0) return 0L
        return BigDecimal.valueOf(remainingCents)
            .divide(BigDecimal.valueOf(daysRemaining.toLong()), 0, RoundingMode.DOWN)
            .longValueExact()
    }

    fun deltaPercentage(currentCents: Long, previousCents: Long): Int? {
        if (previousCents <= 0L) return null
        val diff = BigDecimal.valueOf(currentCents - previousCents)
        return diff.multiply(HUNDRED)
            .divide(BigDecimal.valueOf(previousCents), 0, RoundingMode.HALF_UP)
            .toInt()
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes B"
        val kb = BigDecimal.valueOf(bytes).divide(BigDecimal(1024), 1, RoundingMode.HALF_UP)
        if (kb < BigDecimal(1024)) return "${kb.toPlainString()} KB"
        val mb = kb.divide(BigDecimal(1024), 2, RoundingMode.HALF_UP)
        return "${mb.toPlainString()} MB"
    }
}
