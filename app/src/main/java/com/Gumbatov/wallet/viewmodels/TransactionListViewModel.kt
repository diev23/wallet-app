package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionWithTags
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.launch
import com.Gumbatov.wallet.models.TransactionItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())

    val transactions: LiveData<List<TransactionWithTags>> =
        transactionRepository.transactions.asLiveData()

    val categories: LiveData<List<Category>> =
        categoryRepository.categories.asLiveData()
    /** Rows for the list screen: each transaction joined with its category. */
    val items: LiveData<List<TransactionItem>> = combine(
        transactionRepository.transactions,
        categoryRepository.categories
    ) { transactions, categories ->
        val categoriesById = categories.associateBy { it.id }
        transactions.mapNotNull { withTags ->
            val category = categoriesById[withTags.transaction.categoryId]
                ?: return@mapNotNull null
            TransactionItem(withTags.transaction, category, withTags.tags)
        }
    }.asLiveData()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun add(transaction: Transaction, tagIds: List<Long> = emptyList()) =
        launchSafely { transactionRepository.add(transaction, tagIds) }

    fun update(transaction: Transaction, tagIds: List<Long> = emptyList()) =
        launchSafely { transactionRepository.update(transaction, tagIds) }

    fun delete(transaction: Transaction) =
        launchSafely { transactionRepository.delete(transaction) }

    fun deleteCategory(category: Category) =
        launchSafely { categoryRepository.delete(category) }

    /** Runs a database action in the background; a failure becomes a message, not a crash. */
    private fun launchSafely(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                _error.value = "Не удалось выполнить операцию: ${e.message}"
            }
        }
    }
    // TEMPORARY (day 5): lets us test the list before the form exists (day 6)
    fun addTestTransaction() = launchSafely {
        val category = categoryRepository.categories.first().firstOrNull()
            ?: return@launchSafely
        transactionRepository.add(
            Transaction(
                title = "Test ${System.currentTimeMillis() % 1000}",
                amountMinor = 15000,
                date = System.currentTimeMillis(),
                categoryId = category.id
            ),
            emptyList()
        )
    }
}