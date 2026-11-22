package com.Gumbatov.wallet.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Many-to-many link between transactions and tags. */
@Entity(
    tableName = "transaction_tag",
    primaryKeys = ["transaction_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tag_id")]
)
data class TransactionTagCrossRef(
    @ColumnInfo(name = "transaction_id") val transactionId: Long,
    @ColumnInfo(name = "tag_id") val tagId: Long
)