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
import com.Gumbatov.wallet.models.ListFilter
import com.Gumbatov.wallet.utils.TransactionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import com.Gumbatov.wallet.repository.SettingsRepository

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())

    val transactions: LiveData<List<TransactionWithTags>> =
        transactionRepository.transactions.asLiveData()

    val categories: LiveData<List<Category>> =
        categoryRepository.categories.asLiveData()
    /** Rows for the list screen: each transaction joined with its category. */
    private val filter = MutableStateFlow(ListFilter())

    /** Current search / filter / sort values (the fragment reads them to draw the chips). */
    val currentFilter: ListFilter get() = filter.value

    fun updateFilter(change: (ListFilter) -> ListFilter) {
        filter.value = change(filter.value)
    }

    /** Clears the search text and all filters; the chosen sort order stays. */
    fun resetFilters() {
        filter.value = ListFilter(sort = filter.value.sort)
    }
    private val settingsRepository = SettingsRepository(application)

    init {
        // The sort order from the settings is applied now and every time it changes.
        viewModelScope.launch {
            settingsRepository.defaultSortFlow.collect { sort ->
                filter.value = filter.value.copy(sort = sort)
            }
        }
    }
    /** What the list screen draws: filtered rows + how many rows exist in total. */
    val listState: LiveData<ListState> = combine(
        transactionRepository.transactions,
        categoryRepository.categories,
        filter
    ) { transactions, categories, listFilter ->
        val categoriesById = categories.associateBy { it.id }
        val all = transactions.mapNotNull { withTags ->
            val category = categoriesById[withTags.transaction.categoryId]
                ?: return@mapNotNull null
            TransactionItem(withTags.transaction, category, withTags.tags)
        }
        ListState(items = TransactionFilter.apply(all, listFilter), totalCount = all.size)
    }.asLiveData()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun add(transaction: Transaction, tagIds: List<Long> = emptyList()) =
        launchSafely { transactionRepository.add(transaction, tagIds) }

    fun delete(transaction: Transaction) =
        launchSafely { transactionRepository.delete(transaction) }



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
}
data class ListState(val items: List<TransactionItem>, val totalCount: Int)