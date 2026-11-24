package com.Gumbatov.wallet.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.Gumbatov.wallet.models.Currency
import com.Gumbatov.wallet.models.SortOrder
import com.Gumbatov.wallet.models.ThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Reads and writes the user settings. They are kept between app launches. */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences("wallet_settings", Context.MODE_PRIVATE)

    var theme: ThemeMode
        get() = readEnum(KEY_THEME, ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var currency: Currency
        get() = readEnum(KEY_CURRENCY, Currency.AZN)
        set(value) = prefs.edit().putString(KEY_CURRENCY, value.name).apply()

    var defaultSort: SortOrder
        get() = readEnum(KEY_SORT, SortOrder.DATE_DESC)
        set(value) = prefs.edit().putString(KEY_SORT, value.name).apply()

    /** Emits the current default sort and then every time it changes. */
    val defaultSortFlow: Flow<SortOrder> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SORT) trySend(defaultSort)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(defaultSort)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    /** Applies the saved theme to the whole app (the screen is recreated if it changed). */
    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }

    private inline fun <reified T : Enum<T>> readEnum(key: String, default: T): T {
        val name = prefs.getString(key, null) ?: return default
        return enumValues<T>().firstOrNull { it.name == name } ?: default
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_CURRENCY = "currency"
        const val KEY_SORT = "default_sort"
    }
}