package com.Gumbatov.wallet.models

/** One row of a management list. colorHex is null for rows without a color (tags). */
data class ManageRow(
    val id: Long,
    val title: String,
    val subtitle: String,
    val colorHex: String?
)