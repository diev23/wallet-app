package com.Gumbatov.wallet.utils

import android.content.Context
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.TransactionWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Exports transactions to a CSV table that Excel and Google Sheets can open.
 * The file is created in the app cache folder, so no storage permission is needed.
 */
class CsvExporter(private val context: Context) {

    /** Writes the file on a background thread and returns it. */
    suspend fun export(items: List<TransactionWithTags>, categories: Map<Long, Category>): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() } // remove the old exports
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "wallet_$stamp.csv")
            file.writeText(buildCsv(items, categories), Charsets.UTF_8)
            file
        }

    /** Builds the whole table as text: BOM, header line and one line per transaction. */
    fun buildCsv(items: List<TransactionWithTags>, categories: Map<Long, Category>): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val incomeLabel = context.getString(R.string.type_income)
        val expenseLabel = context.getString(R.string.type_expense)

        val builder = StringBuilder("﻿") // BOM: Excel reads the file as UTF-8
        builder.append(
            line(
                listOf(
                    context.getString(R.string.csv_col_date),
                    context.getString(R.string.csv_col_type),
                    context.getString(R.string.csv_col_category),
                    context.getString(R.string.csv_col_title),
                    context.getString(R.string.csv_col_amount) + " (" + Formatters.currencySymbol + ")",
                    context.getString(R.string.csv_col_tags),
                    context.getString(R.string.csv_col_description)
                )
            )
        )
        for (item in items) {
            val transaction = item.transaction
            val category = categories[transaction.categoryId]
            builder.append(
                line(
                    listOf(
                        dateFormat.format(Date(transaction.date)),
                        if (category?.isIncome == true) incomeLabel else expenseLabel,
                        category?.name.orEmpty(),
                        transaction.title,
                        plainAmount(transaction.amountMinor),
                        item.tags.joinToString(", ") { it.name },
                        transaction.description
                    )
                )
            )
        }
        return builder.toString()
    }

    /** 123450 -> "1234,50": decimal comma, no currency symbol and no spaces. */
    private fun plainAmount(amountMinor: Long): String {
        val value = abs(amountMinor)
        return "${value / 100},${(value % 100).toString().padStart(2, '0')}"
    }

    private fun line(cells: List<String>): String = cells.joinToString(";", postfix = "\r\n") { escape(it) }

    /** Quotes a cell if it contains a separator, a quote or a line break. */
    private fun escape(value: String): String {
        // A text that starts with = + - @ would be run as a formula by Excel
        val text = if (value.isNotEmpty() && value[0] in "=+-@") "'" + value else value
        val needsQuotes = text.any { it == ';' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + text.replace("\"", "\"\"") + "\"" else text
    }

    private companion object {
        const val EXPORT_DIR = "exports"
    }
}