package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.repository.CategoryRepository
import com.Gumbatov.wallet.repository.TransactionRepository
import com.Gumbatov.wallet.utils.CsvExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

/** Prepares the CSV file with all transactions; the screen only shows the result. */
class ExportViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val transactionRepository = TransactionRepository(database)
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val exporter = CsvExporter(application)

    private val _exporting = MutableLiveData(false)
    val exporting: LiveData<Boolean> = _exporting

    /** A ready file to share. The screen calls fileHandled() after it was used. */
    private val _file = MutableLiveData<File?>()
    val file: LiveData<File?> = _file

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun export() {
        if (_exporting.value == true) return // a second tap is ignored while the first export runs
        viewModelScope.launch {
            _exporting.value = true
            try {
                val items = transactionRepository.transactions.first()
                if (items.isEmpty()) {
                    _message.value = text(R.string.export_empty)
                    return@launch
                }
                val categories = categoryRepository.categories.first().associateBy { it.id }
                _file.value = exporter.export(items, categories)
            } catch (e: Exception) {
                // Never crash: any failure becomes a message
                _message.value = text(R.string.export_failed)
            } finally {
                _exporting.value = false
            }
        }
    }

    fun fileHandled() {
        _file.value = null
    }

    fun messageShown() {
        _message.value = null
    }

    private fun text(resId: Int): String = getApplication<Application>().getString(resId)
}