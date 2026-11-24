package com.Gumbatov.wallet.utils

import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.CategoryShare
import com.Gumbatov.wallet.models.MonthTotals
import com.Gumbatov.wallet.models.StatsPeriod
import com.Gumbatov.wallet.models.StatsState
import com.Gumbatov.wallet.models.TransactionWithTags
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Calculates the numbers and chart data of the statistics screen. */
object StatsCalculator {

    private const val MONTHS_IN_CHART = 6

    fun calculate(
        transactions: List<TransactionWithTags>,
        categories: List<Category>,
        period: StatsPeriod,
        nowMillis: Long
    ): StatsState {
        val categoriesById = categories.associateBy { it.id }
        val periodStart = startOfDay(nowMillis, -(period.days - 1))
        val periodEnd = startOfDay(nowMillis, 1) // start of tomorrow, exclusive

        var income = 0L
        var expense = 0L
        var count = 0
        val expenseByCategory = mutableMapOf<Long, Long>()

        for (item in transactions) {
            val t = item.transaction
            val category = categoriesById[t.categoryId] ?: continue
            if (t.date < periodStart || t.date >= periodEnd) continue
            count++
            if (category.isIncome) {
                income += t.amountMinor
            } else {
                expense += t.amountMinor
                expenseByCategory[category.id] = (expenseByCategory[category.id] ?: 0L) + t.amountMinor
            }
        }

        val shares = expenseByCategory
            .map { (id, sum) -> CategoryShare(categoriesById.getValue(id), sum) }
            .sortedByDescending { it.amountMinor }

        return StatsState(
            incomeMinor = income,
            expenseMinor = expense,
            balanceMinor = income - expense,
            averageExpensePerDayMinor = expense / period.days,
            expenseByCategory = shares,
            months = monthlyTotals(transactions, categoriesById, nowMillis),
            transactionCount = count
        )
    }

    /** Income and expense for each of the last 6 calendar months, oldest first. */
    private fun monthlyTotals(
        transactions: List<TransactionWithTags>,
        categoriesById: Map<Long, Category>,
        nowMillis: Long
    ): List<MonthTotals> {
        val labelFormat = SimpleDateFormat("MM.yy", Locale.getDefault())
        val result = mutableListOf<MonthTotals>()
        for (back in MONTHS_IN_CHART - 1 downTo 0) {
            val start = startOfMonth(nowMillis, -back)
            val end = startOfMonth(nowMillis, -back + 1)
            var income = 0L
            var expense = 0L
            for (item in transactions) {
                val t = item.transaction
                if (t.date < start || t.date >= end) continue
                val category = categoriesById[t.categoryId] ?: continue
                if (category.isIncome) income += t.amountMinor else expense += t.amountMinor
            }
            result.add(MonthTotals(labelFormat.format(Date(start)), income, expense))
        }
        return result
    }

    /** Local midnight of the day that is [plusDays] away from [millis]. */
    private fun startOfDay(millis: Long, plusDays: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, plusDays)
        }.timeInMillis

    /** Local midnight of the first day of the month that is [plusMonths] away from [millis]. */
    private fun startOfMonth(millis: Long, plusMonths: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, plusMonths)
        }.timeInMillis
}