package com.Gumbatov.wallet.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.databinding.FragmentSettingsBinding
import com.Gumbatov.wallet.models.Currency
import com.Gumbatov.wallet.models.SortOrder
import com.Gumbatov.wallet.models.ThemeMode
import com.Gumbatov.wallet.viewmodels.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Settings screen: theme, currency and the default sort order. */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        renderValues()

        binding.rowTheme.setOnClickListener {
            showChoiceDialog(
                title = R.string.settings_theme,
                options = ThemeMode.entries,
                selected = viewModel.theme,
                label = { getString(themeLabel(it)) }
            ) { mode ->
                viewModel.setTheme(mode)
                renderValues()
            }
        }
        binding.rowCurrency.setOnClickListener {
            showChoiceDialog(
                title = R.string.settings_currency,
                options = Currency.entries,
                selected = viewModel.currency,
                label = { getString(currencyLabel(it)) }
            ) { currency ->
                viewModel.setCurrency(currency)
                renderValues()
            }
        }
        binding.rowSort.setOnClickListener {
            showChoiceDialog(
                title = R.string.settings_default_sort,
                options = SortOrder.entries,
                selected = viewModel.defaultSort,
                label = { getString(sortLabel(it)) }
            ) { order ->
                viewModel.setDefaultSort(order)
                renderValues()
            }
        }
    }

    /** Writes the current values under the setting names. */
    private fun renderValues() {
        binding.valueTheme.text = getString(themeLabel(viewModel.theme))
        binding.valueCurrency.text = getString(currencyLabel(viewModel.currency))
        binding.valueSort.text = getString(sortLabel(viewModel.defaultSort))
    }

    /** One dialog for all single-choice settings. */
    private fun <T> showChoiceDialog(
        title: Int,
        options: List<T>,
        selected: T,
        label: (T) -> String,
        onChosen: (T) -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(
                options.map(label).toTypedArray(),
                options.indexOf(selected)
            ) { dialog, which ->
                onChosen(options[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun themeLabel(mode: ThemeMode): Int = when (mode) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }

    private fun currencyLabel(currency: Currency): Int = when (currency) {
        Currency.AZN -> R.string.currency_azn
        Currency.RUB -> R.string.currency_rub
        Currency.USD -> R.string.currency_usd
        Currency.EUR -> R.string.currency_eur
    }

    // The same labels as in the list screen; day 12: move to one shared place.
    private fun sortLabel(order: SortOrder): Int = when (order) {
        SortOrder.DATE_DESC -> R.string.sort_date_desc
        SortOrder.DATE_ASC -> R.string.sort_date_asc
        SortOrder.AMOUNT_DESC -> R.string.sort_amount_desc
        SortOrder.AMOUNT_ASC -> R.string.sort_amount_asc
        SortOrder.TITLE_ASC -> R.string.sort_title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}