package com.Gumbatov.wallet.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    // @Transaction is required for queries with relations: Room reads several tables atomically.
    // Full name is used because our entity is also called Transaction.
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithTags(): Flow<List<TransactionWithTags>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getWithTagsById(id: Long): TransactionWithTags?
}