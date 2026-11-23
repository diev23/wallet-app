package com.Gumbatov.wallet.utils

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Helpers that turn stored values into text for the screen. */
object Formatters {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    /** 1700000000000 -> "14.11.2023" */
    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    /** Amount in kopecks to text: 15000 -> "-150,00" for an expense, "+150,00" for income. */
    fun formatAmount(amountMinor: Long, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        val value = BigDecimal.valueOf(amountMinor, 2)
        return sign + String.format(Locale.getDefault(), "%,.2f", value)
    }
}