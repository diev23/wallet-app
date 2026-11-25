package com.Gumbatov.wallet.utils

import kotlin.math.abs

/** Pure formatting rules of the CSV file. No Android classes, so it is easy to test. */
object CsvFormat {

    private const val SEPARATOR = ";"
    private const val LINE_END = "\r\n"

    /** 123450 -> "1234,50": decimal comma, no currency symbol and no spaces. */
    fun amount(amountMinor: Long): String {
        val value = abs(amountMinor)
        return "${value / 100},${(value % 100).toString().padStart(2, '0')}"
    }

    /** Joins the cells into one line of the file. */
    fun line(cells: List<String>): String = cells.joinToString(SEPARATOR, postfix = LINE_END) { escape(it) }

    /** Quotes a cell if it contains a separator, a quote or a line break. */
    fun escape(value: String): String {
        // A text that starts with = + - @ would be run as a formula by Excel
        val text = if (value.isNotEmpty() && value[0] in "=+-@") "'" + value else value
        val needsQuotes = text.any { it == ';' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + text.replace("\"", "\"\"") + "\"" else text
    }
}