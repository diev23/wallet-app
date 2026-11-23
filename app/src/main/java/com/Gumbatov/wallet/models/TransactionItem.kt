package com.Gumbatov.wallet.models

/** One row of the transaction list: the transaction, its category and its tags. */
data class TransactionItem(
    val transaction: Transaction,
    val category: Category,
    val tags: List<Tag>
)