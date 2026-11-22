package com.Gumbatov.wallet.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.Gumbatov.wallet.models.TransactionTagCrossRef

@Dao
interface TransactionTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: TransactionTagCrossRef)

    @Query("DELETE FROM transaction_tag WHERE transaction_id = :transactionId")
    suspend fun deleteForTransaction(transactionId: Long)
}