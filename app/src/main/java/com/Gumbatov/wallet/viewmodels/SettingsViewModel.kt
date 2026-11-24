package com.Gumbatov.wallet.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.Gumbatov.wallet.models.Currency
import com.Gumbatov.wallet.models.SortOrder
import com.Gumbatov.wallet.models.ThemeMode
import com.Gumbatov.wallet.repository.SettingsRepository
import com.Gumbatov.wallet.utils.Formatters

/** Gives the settings screen the current values and saves the changes. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val theme: ThemeMode get() = repository.theme
    val currency: Currency get() = repository.currency
    val defaultSort: SortOrder get() = repository.defaultSort

    fun setTheme(mode: ThemeMode) {
        repository.theme = mode
        repository.applyTheme() // the screen is recreated with the new theme
    }

    fun setCurrency(newCurrency: Currency) {
        repository.currency = newCurrency
        Formatters.currencySymbol = newCurrency.symbol
    }

    fun setDefaultSort(order: SortOrder) {
        repository.defaultSort = order
    }
}