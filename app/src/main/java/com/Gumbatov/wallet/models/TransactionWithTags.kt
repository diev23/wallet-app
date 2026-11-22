package com.Gumbatov.wallet.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** A transaction together with all its tags (many-to-many via transaction_tag). */
data class TransactionWithTags(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transaction_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<Tag>
)