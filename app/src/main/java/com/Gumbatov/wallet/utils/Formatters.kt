package com.Gumbatov.wallet.utils

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.math.RoundingMode

/** Helpers that turn stored values into text for the screen. */
object Formatters {
    /** Максимальная сумма: 1 000 000 000,00 в копейках. Защита от переполнения. */
    private const val MAX_AMOUNT_MINOR = 100_000_000_000L

    /** 15050 -> "150,50"; a negative balance keeps its minus sign. */
    /** Symbol of the selected currency; set at start and when the setting changes. */
    var currencySymbol: String = "₼"

    /** Amount in kopecks to text: 15000 -> "-150,00 ₼" for an expense, "+150,00 ₼" for income. */
    fun formatAmount(amountMinor: Long, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        val value = BigDecimal.valueOf(amountMinor, 2)
        return sign + String.format(Locale.getDefault(), "%,.2f", value) + " " + currencySymbol
    }

    /** 15050 -> "150,50 ₼"; a negative balance keeps its minus sign. */
    fun formatMoney(amountMinor: Long): String =
        String.format(Locale.getDefault(), "%,.2f", BigDecimal.valueOf(amountMinor, 2)) +
                " " + currencySymbol


    /** "150,5" or "150.50" -> 15050. Returns null if the text is not a valid amount. */
    fun parseAmountMinor(text: String): Long? {
        val normalized = text.trim().replace(',', '.').replace(" ", "")
        if (normalized.isEmpty()) return null
        return try {
            val minor = BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
            if (minor > MAX_AMOUNT_MINOR) null else minor
        } catch (e: NumberFormatException) {
            null
        } catch (e: ArithmeticException) {
            null
        }
    }

    /** 15050 -> "150.50" (for filling the edit field). */
    fun amountToInput(amountMinor: Long): String =
        BigDecimal.valueOf(amountMinor, 2).toPlainString()

    /** MaterialDatePicker returns UTC midnight; convert it to local midnight. */
    fun pickerToLocal(utcMillis: Long): Long =
        utcMillis - TimeZone.getDefault().getOffset(utcMillis)

    /** Reverse conversion: show the stored local date correctly in the picker. */
    fun localToPicker(localMillis: Long): Long =
        localMillis + TimeZone.getDefault().getOffset(localMillis)

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    /** 1700000000000 -> "14.11.2023" */
    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    /** Amount in kopecks to text: 15000 -> "-150,00" for an expense, "+150,00" for income. */

    }