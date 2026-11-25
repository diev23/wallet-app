package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Category together with the number of its transactions. */
data class CategoryRow(val category: Category, val transactionCount: Int)

/** Logic of the categories screen: list, create / update, delete. */
class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val transactionRepository = TransactionRepository(database)

    val rows: LiveData<List<CategoryRow>> = combine(
        categoryRepository.categories,
        transactionRepository.transactions
    ) { categories, transactions ->
        val counts = transactions.groupingBy { it.transaction.categoryId }.eachCount()
        categories.map { CategoryRow(it, counts[it.id] ?: 0) }
    }.asLiveData()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** id == 0 means a new category, otherwise an existing one is updated. */
    fun save(category: Category) = launchSafely {
        if (category.id == 0L) categoryRepository.add(category) else categoryRepository.update(category)
    }

    /** The database also deletes all transactions of the category (ON DELETE CASCADE). */
    fun delete(category: Category) = launchSafely { categoryRepository.delete(category) }

    fun errorShown() {
        _error.value = null
    }

    private fun launchSafely(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                _error.value = "Не удалось выполнить операцию: ${e.message}"
            }
        }
    }
}