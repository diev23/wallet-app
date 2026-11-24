package com.Gumbatov.wallet.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.adapters.TransactionAdapter
import com.Gumbatov.wallet.databinding.FragmentTransactionListBinding
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.ListFilter
import com.Gumbatov.wallet.models.SortOrder
import com.Gumbatov.wallet.models.TypeFilter
import com.Gumbatov.wallet.utils.Formatters
import com.Gumbatov.wallet.viewmodels.ListState
import com.Gumbatov.wallet.viewmodels.TransactionListViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** Main screen: search, filters, sorting and the list of transactions. */
class TransactionListFragment : Fragment(R.layout.fragment_transaction_list) {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionListViewModel by viewModels()

    private var categories: List<Category> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransactionListBinding.bind(view)

        val adapter = TransactionAdapter { item ->
            val args = bundleOf("transactionId" to item.transaction.id)
            findNavController().navigate(R.id.action_list_to_detail, args)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener { onAddClicked() }
        setupSearch()
        setupChips()

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categories = list
            renderChips()
        }
        viewModel.listState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.items)
            renderList(state)
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun onAddClicked() {
        findNavController().navigate(R.id.action_list_to_edit)
    }

    // ---------- search ----------

    private fun setupSearch() {
        // Restore the text after rotation, then start listening.
        binding.searchInput.setText(viewModel.currentFilter.query)
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.updateFilter { it.copy(query = text?.toString().orEmpty()) }
        }
    }

    // ---------- chips and dialogs ----------

    private fun setupChips() {
        binding.chipType.setOnClickListener { showTypeDialog() }
        binding.chipType.setOnCloseIconClickListener {
            changeFilter { it.copy(type = TypeFilter.ALL) }
        }
        binding.chipCategory.setOnClickListener { showCategoryDialog() }
        binding.chipCategory.setOnCloseIconClickListener {
            changeFilter { it.copy(categoryId = null) }
        }
        binding.chipPeriod.setOnClickListener { showPeriodPicker() }
        binding.chipPeriod.setOnCloseIconClickListener {
            changeFilter { it.copy(fromDate = null, toDate = null) }
        }
        binding.chipSort.setOnClickListener { showSortDialog() }
        renderChips()
    }

    private fun changeFilter(change: (ListFilter) -> ListFilter) {
        viewModel.updateFilter(change)
        renderChips()
    }

    /** Writes the current filter values on the chips. */
    private fun renderChips() {
        val filter = viewModel.currentFilter

        binding.chipType.text = getString(
            when (filter.type) {
                TypeFilter.ALL -> R.string.filter_type_all
                TypeFilter.EXPENSE -> R.string.filter_type_expense
                TypeFilter.INCOME -> R.string.filter_type_income
            }
        )
        binding.chipType.isCloseIconVisible = filter.type != TypeFilter.ALL

        val category = categories.firstOrNull { it.id == filter.categoryId }
        binding.chipCategory.text = category?.name ?: getString(R.string.filter_category_all)
        binding.chipCategory.isCloseIconVisible = filter.categoryId != null

        val from = filter.fromDate
        val to = filter.toDate
        binding.chipPeriod.text = if (from != null && to != null) {
            "${Formatters.formatDate(from)} – ${Formatters.formatDate(to)}"
        } else {
            getString(R.string.filter_period)
        }
        binding.chipPeriod.isCloseIconVisible = from != null

        binding.chipSort.text = getString(sortLabel(filter.sort))
    }

    private fun sortLabel(order: SortOrder): Int = when (order) {
        SortOrder.DATE_DESC -> R.string.sort_date_desc
        SortOrder.DATE_ASC -> R.string.sort_date_asc
        SortOrder.AMOUNT_DESC -> R.string.sort_amount_desc
        SortOrder.AMOUNT_ASC -> R.string.sort_amount_asc
        SortOrder.TITLE_ASC -> R.string.sort_title
    }

    private fun showTypeDialog() {
        val types = TypeFilter.entries
        val labels = arrayOf(
            getString(R.string.filter_type_all),
            getString(R.string.filter_type_expense),
            getString(R.string.filter_type_income)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_type)
            .setSingleChoiceItems(labels, viewModel.currentFilter.type.ordinal) { dialog, which ->
                changeFilter { it.copy(type = types[which]) }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCategoryDialog() {
        val labels = (listOf(getString(R.string.filter_category_all)) + categories.map { it.name })
            .toTypedArray()
        val selectedId = viewModel.currentFilter.categoryId
        val checked = categories.indexOfFirst { it.id == selectedId } + 1 // 0 = "all"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_category)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                val id = if (which == 0) null else categories[which - 1].id
                changeFilter { it.copy(categoryId = id) }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSortDialog() {
        val orders = SortOrder.entries
        val labels = orders.map { getString(sortLabel(it)) }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_sort)
            .setSingleChoiceItems(labels, viewModel.currentFilter.sort.ordinal) { dialog, which ->
                changeFilter { it.copy(sort = orders[which]) }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPeriodPicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.filter_period)
        val from = viewModel.currentFilter.fromDate
        val to = viewModel.currentFilter.toDate
        if (from != null && to != null) {
            builder.setSelection(
                Pair(Formatters.localToPicker(from), Formatters.localToPicker(to))
            )
        }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { range ->
            changeFilter {
                it.copy(
                    fromDate = Formatters.pickerToLocal(range.first),
                    toDate = Formatters.pickerToLocal(range.second)
                )
            }
        }
        picker.show(childFragmentManager, "period_picker")
    }

    // ---------- list and empty states ----------

    private fun renderList(state: ListState) {
        binding.progressBar.visibility = View.GONE
        val isEmpty = state.items.isEmpty()
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (!isEmpty) return

        // Records exist but nothing matches -> "nothing found"; no records at all -> "add the first".
        val nothingFound = state.totalCount > 0
        binding.emptyTitle.setText(if (nothingFound) R.string.not_found_title else R.string.empty_title)
        binding.emptyHint.setText(if (nothingFound) R.string.not_found_hint else R.string.empty_hint)
        binding.emptyAddButton.setText(if (nothingFound) R.string.reset_filters else R.string.empty_button)
        binding.emptyAddButton.setOnClickListener {
            if (nothingFound) {
                viewModel.resetFilters()
                binding.searchInput.setText("")
                renderChips()
            } else {
                onAddClicked()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}