package com.Gumbatov.wallet.models

/** Period of the statistics: the last N days including today. */
enum class StatsPeriod(val days: Int) {
    WEEK(7),
    MONTH(30),
    YEAR(365)
}

/** One slice of the pie chart: how much was spent in a category. */
data class CategoryShare(val category: Category, val amountMinor: Long)

/** One group of bars in the bar chart. */
data class MonthTotals(val label: String, val incomeMinor: Long, val expenseMinor: Long)

/** Everything the statistics screen draws. Money is in minor units (kopecks). */
data class StatsState(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val balanceMinor: Long,
    val averageExpensePerDayMinor: Long,
    val expenseByCategory: List<CategoryShare>,
    val months: List<MonthTotals>,
    val transactionCount: Int
)