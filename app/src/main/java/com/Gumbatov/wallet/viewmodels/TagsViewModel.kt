package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.models.Tag
import com.Gumbatov.wallet.repository.TagRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Tag together with the number of transactions that use it. */
data class TagRow(val tag: Tag, val usageCount: Int)

/** Logic of the tags screen: list, add, delete. */
class TagsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val tagRepository = TagRepository(database.tagDao())
    private val transactionRepository = TransactionRepository(database)

    val rows: LiveData<List<TagRow>> = combine(
        tagRepository.tags,
        transactionRepository.transactions
    ) { tags, transactions ->
        val counts = mutableMapOf<Long, Int>()
        for (item in transactions) {
            for (tag in item.tags) counts[tag.id] = (counts[tag.id] ?: 0) + 1
        }
        tags.map { TagRow(it, counts[it.id] ?: 0) }
    }.asLiveData()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun add(name: String) {
        viewModelScope.launch {
            try {
                tagRepository.add(Tag(name = name))
            } catch (e: Exception) {
                // The tag name is UNIQUE in the database.
                _error.value = "Такой тег уже существует"
            }
        }
    }

    fun delete(tag: Tag) {
        viewModelScope.launch {
            try {
                tagRepository.delete(tag)
            } catch (e: Exception) {
                _error.value = "Не удалось удалить тег"
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }
}