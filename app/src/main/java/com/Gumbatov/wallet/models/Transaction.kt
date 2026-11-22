package com.Gumbatov.wallet.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Main entity: one income or expense record. Deleted together with its category. */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("category_id")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,   // amount in minor units (kopecks)
    val date: Long,                                             // epoch millis
    @ColumnInfo(name = "category_id") val categoryId: Long
)