package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Tag
import com.Gumbatov.wallet.models.Transaction
import com.Gumbatov.wallet.models.TransactionWithTags
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TagRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.launch

/** State and logic of the add/edit form. */
class TransactionEditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val tagRepository = TagRepository(database.tagDao())

    val categories: LiveData<List<Category>> = categoryRepository.categories.asLiveData()
    val tags: LiveData<List<Tag>> = tagRepository.tags.asLiveData()

    // Form state (survives screen rotation)
    var isIncome: Boolean = false
    var categoryId: Long? = null
    var dateMillis: Long = System.currentTimeMillis()
    val selectedTagIds: MutableSet<Long> = mutableSetOf()
    var textFilled: Boolean = false

    private val _loaded = MutableLiveData<TransactionWithTags?>()
    val loaded: LiveData<TransactionWithTags?> = _loaded

    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var started = false

    /** Called once from the fragment. For id == 0 the form stays empty (create mode). */
    fun start(transactionId: Long) {
        if (started) return
        started = true
        if (transactionId == 0L) {
            textFilled = true // nothing to prefill
            return
        }
        viewModelScope.launch {
            try {
                val item = transactionRepository.getById(transactionId) ?: return@launch
                val category = database.categoryDao().getById(item.transaction.categoryId)
                isIncome = category?.isIncome ?: false
                categoryId = item.transaction.categoryId
                dateMillis = item.transaction.date
                selectedTagIds.clear()
                selectedTagIds.addAll(item.tags.map { it.id })
                _loaded.value = item
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить запись"
            }
        }
    }

    fun save(id: Long, title: String, description: String, amountMinor: Long, categoryId: Long) {
        val transaction = Transaction(
            id = id,
            title = title,
            description = description,
            amountMinor = amountMinor,
            date = dateMillis,
            categoryId = categoryId
        )
        val tagIds = selectedTagIds.toList()
        viewModelScope.launch {
            _saving.value = true
            try {
                if (id == 0L) transactionRepository.add(transaction, tagIds)
                else transactionRepository.update(transaction, tagIds)
                _saved.value = true
            } catch (e: Exception) {
                // Never crash on a failed save: show a message instead.
                _error.value = "Не удалось сохранить запись"
            } finally {
                _saving.value = false
            }
        }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            try {
                val id = tagRepository.add(Tag(name = name))
                selectedTagIds.add(id) // the new tag is selected right away
            } catch (e: Exception) {
                _error.value = "Такой тег уже существует"
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }
}