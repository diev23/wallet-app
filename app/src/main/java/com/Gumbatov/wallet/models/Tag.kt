package com.Gumbatov.wallet.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A free-form label that can be attached to many transactions. */
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)