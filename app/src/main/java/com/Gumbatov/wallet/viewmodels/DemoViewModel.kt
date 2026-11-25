package com.Gumbatov.wallet.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.Gumbatov.wallet.database.AppDatabase
import com.Gumbatov.wallet.utils.DemoData
import kotlinx.coroutines.launch

/** Debug tool: adds random operations so that the app can be tested on big data. */
class DemoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean> = _busy

    fun fill(count: Int) {
        if (_busy.value == true) return
        viewModelScope.launch {
            _busy.value = true
            try {
                DemoData.generate(database, count)
            } catch (e: Exception) {
                Log.e("DemoViewModel", "Could not add demo data", e)
            } finally {
                _busy.value = false
            }
        }
    }
}