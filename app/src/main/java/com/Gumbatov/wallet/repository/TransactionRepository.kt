package com.Gumbatov.wallet.repository

import androidx.room.withTransaction
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionTagCrossRef
import com.Gumbatov.wallet.models.TransactionWithTags
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val db: AppDatabase) {
    private val transactionDao = db.transactionDao()
    private val crossRefDao = db.transactionTagDao()

    val transactions: Flow<List<TransactionWithTags>> = transactionDao.getAllWithTags()

    suspend fun getById(id: Long): TransactionWithTags? = transactionDao.getWithTagsById(id)


    /** Saves a new transaction and links its tags in one database transaction. */
    suspend fun add(transaction: Transaction, tagIds: List<Long>): Long =
        db.withTransaction {
            val id = transactionDao.insert(transaction)
            tagIds.forEach { crossRefDao.insert(TransactionTagCrossRef(id, it)) }
            id
        }

    /** Updates the transaction and replaces its set of tags. */
    suspend fun update(transaction: Transaction, tagIds: List<Long>) =
        db.withTransaction {
            transactionDao.update(transaction)
            crossRefDao.deleteForTransaction(transaction.id)
            tagIds.forEach { crossRefDao.insert(TransactionTagCrossRef(transaction.id, it)) }
        }

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    fun observe(id: Long): Flow<TransactionWithTags?> =
        transactionDao.observeWithTagsById(id)
}