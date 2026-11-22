package com.Gumbatov.wallet.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Income or expense category, e.g. "Food" or "Salary". */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String,                                   // hex, e.g. "#FF9800"
    @ColumnInfo(name = "is_income") val isIncome: Boolean
)