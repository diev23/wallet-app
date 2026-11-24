package com.Gumbatov.wallet.utils

import com.Gumbatov.wallet.models.ListFilter
import com.Gumbatov.wallet.models.SortOrder
import com.Gumbatov.wallet.models.TransactionItem
import com.Gumbatov.wallet.models.TypeFilter

/** Search, filtering and sorting of the transaction list. */
object TransactionFilter {

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    /** Applies the search text and all filters together (logical AND), then sorts. */
    fun apply(all: List<TransactionItem>, filter: ListFilter): List<TransactionItem> {
        val query = filter.query.trim()
        val filtered = all.filter { item ->
            val t = item.transaction
            matchesQuery(item, query) &&
                    matchesType(item, filter.type) &&
                    (filter.categoryId == null || t.categoryId == filter.categoryId) &&
                    (filter.fromDate == null || t.date >= filter.fromDate) &&
                    (filter.toDate == null || t.date < filter.toDate + DAY_MILLIS)
        }
        return sort(filtered, filter.sort)
    }

    /** The text is searched in the title, the description and the tag names. */
    private fun matchesQuery(item: TransactionItem, query: String): Boolean {
        if (query.isEmpty()) return true
        val t = item.transaction
        return t.title.contains(query, ignoreCase = true) ||
                t.description.contains(query, ignoreCase = true) ||
                item.tags.any { it.name.contains(query, ignoreCase = true) }
    }

    private fun matchesType(item: TransactionItem, type: TypeFilter): Boolean =
        type == TypeFilter.ALL || (type == TypeFilter.INCOME) == item.category.isIncome

    private fun sort(items: List<TransactionItem>, order: SortOrder): List<TransactionItem> =
        when (order) {
            SortOrder.DATE_DESC -> items.sortedWith(
                compareByDescending<TransactionItem> { it.transaction.date }
                    .thenByDescending { it.transaction.id }
            )
            SortOrder.DATE_ASC -> items.sortedWith(
                compareBy<TransactionItem> { it.transaction.date }
                    .thenBy { it.transaction.id }
            )
            SortOrder.AMOUNT_DESC -> items.sortedByDescending { it.transaction.amountMinor }
            SortOrder.AMOUNT_ASC -> items.sortedBy { it.transaction.amountMinor }
            SortOrder.TITLE_ASC -> items.sortedBy { it.transaction.title.lowercase() }
        }
}