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

    // Получить все операции
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    // Получить операцию по ID
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    // Получить все операции вместе с тегами
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithTags(): Flow<List<TransactionWithTags>>

    // Получить одну операцию вместе с тегами
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getWithTagsById(id: Long): TransactionWithTags?

    // Наблюдать за одной операцией вместе с тегами
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeWithTagsById(id: Long): Flow<TransactionWithTags?>
}