package com.Gumbatov.wallet.models

/** Which kind of transactions to show. The order matches the items of the dialog. */
enum class TypeFilter { ALL, EXPENSE, INCOME }

/** How the list is sorted. */
enum class SortOrder { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, TITLE_ASC }

/** Everything the user can set above the list. Empty / null means "no restriction". */
data class ListFilter(
    val query: String = "",
    val type: TypeFilter = TypeFilter.ALL,
    val categoryId: Long? = null,
    val fromDate: Long? = null,   // local midnight of the first day, inclusive
    val toDate: Long? = null,     // local midnight of the last day, inclusive
    val sort: SortOrder = SortOrder.DATE_DESC
)