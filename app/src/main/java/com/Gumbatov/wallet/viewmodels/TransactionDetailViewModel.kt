package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionWithTags
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.launch

/** Shows one transaction and deletes it. Updates automatically after editing. */
class TransactionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())

    private val transactionId = MutableLiveData<Long>()

    val item: LiveData<TransactionWithTags?> = transactionId.switchMap { id ->
        transactionRepository.observe(id).asLiveData()
    }
    val categories: LiveData<List<Category>> = categoryRepository.categories.asLiveData()

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> = _deleted

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun setId(id: Long) {
        if (transactionId.value != id) transactionId.value = id
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.delete(transaction)
                _deleted.value = true
            } catch (e: Exception) {
                _error.value = "Не удалось удалить запись"
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }
}