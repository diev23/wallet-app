package com.Gumbatov.wallet.ui.edit

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.databinding.FragmentTransactionEditBinding
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.Tag
import com.Gumbatov.wallet.utils.Formatters
import com.Gumbatov.wallet.viewmodels.TransactionEditViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/** Form for creating (id == 0) or editing an existing transaction. */
class TransactionEditFragment : Fragment(R.layout.fragment_transaction_edit) {

    private var _binding: FragmentTransactionEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionEditViewModel by viewModels()

    private var editId = 0L
    private var allCategories: List<Category> = emptyList()
    private var shownCategories: List<Category> = emptyList()
    private var allTags: List<Tag> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransactionEditBinding.bind(view)

        editId = arguments?.getLong("transactionId", 0L) ?: 0L
        viewModel.start(editId)

        binding.editHeader.setText(if (editId == 0L) R.string.edit_new else R.string.edit_change)

        renderFormState()
        setupInputs()
        setupObservers()
    }

    /** Shows type and date from the ViewModel (also after rotation). */
    private fun renderFormState() {
        binding.typeToggle.check(
            if (viewModel.isIncome) R.id.toggle_income else R.id.toggle_expense
        )
        binding.editDate.setText(Formatters.formatDate(viewModel.dateMillis))
    }

    private fun setupInputs() {
        // Clear the red error as soon as the user starts fixing the field.
        binding.editTitle.doOnTextChanged { _, _, _, _ -> binding.titleLayout.error = null }
        binding.editAmount.doOnTextChanged { _, _, _, _ -> binding.amountLayout.error = null }

        binding.typeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.isIncome = checkedId == R.id.toggle_income
                refreshCategories()
            }
        }

        binding.editDate.setOnClickListener { showDatePicker() }
        binding.buttonAddTag.setOnClickListener { showAddTagDialog() }
        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { onSaveClicked() }

        binding.editCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.categoryId = shownCategories[position].id
            binding.categoryLayout.error = null
        }
    }

    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            allCategories = list
            refreshCategories()
        }
        viewModel.tags.observe(viewLifecycleOwner) { list ->
            allTags = list
            showTags()
        }
        viewModel.loaded.observe(viewLifecycleOwner) { item ->
            if (item == null) return@observe
            // Fill text fields only once, otherwise a rotation would erase user input.
            if (!viewModel.textFilled) {
                viewModel.textFilled = true
                binding.editTitle.setText(item.transaction.title)
                binding.editAmount.setText(Formatters.amountToInput(item.transaction.amountMinor))
                binding.editDescription.setText(item.transaction.description)
            }
            renderFormState()
            refreshCategories()
            showTags()
        }
        viewModel.saving.observe(viewLifecycleOwner) { saving ->
            binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
            binding.buttonSave.isEnabled = !saving
        }
        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) findNavController().popBackStack()
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.errorShown()
                showTags()
            }
        }
    }

    /** Dropdown shows only categories of the chosen type (income / expense). */
    private fun refreshCategories() {
        shownCategories = allCategories.filter { it.isIncome == viewModel.isIncome }
        val selected = shownCategories.firstOrNull { it.id == viewModel.categoryId }
        if (selected == null) viewModel.categoryId = null

        binding.editCategory.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                shownCategories.map { it.name }
            )
        )
        binding.editCategory.setText(selected?.name ?: "", false)
    }

    private fun showTags() {
        binding.tagGroup.removeAllViews()
        binding.noTagsHint.visibility = if (allTags.isEmpty()) View.VISIBLE else View.GONE
        allTags.forEach { tag ->
            val chip = layoutInflater.inflate(R.layout.item_tag_chip, binding.tagGroup, false) as Chip
            chip.text = tag.name
            chip.isChecked = tag.id in viewModel.selectedTagIds
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) viewModel.selectedTagIds.add(tag.id)
                else viewModel.selectedTagIds.remove(tag.id)
            }
            binding.tagGroup.addView(chip)
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(Formatters.localToPicker(viewModel.dateMillis))
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            viewModel.dateMillis = Formatters.pickerToLocal(utcMillis)
            binding.editDate.setText(Formatters.formatDate(viewModel.dateMillis))
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun showAddTagDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.hint_tag_name)
            setSingleLine()
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_tag)
            .setView(input)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Snackbar.make(binding.root, R.string.error_tag_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.addTag(name)
                }
            }
            .show()
    }

    private fun onSaveClicked() {
        val title = binding.editTitle.text.toString().trim()
        val amount = Formatters.parseAmountMinor(binding.editAmount.text.toString())
        val categoryId = viewModel.categoryId

        var valid = true
        if (title.isEmpty()) {
            binding.titleLayout.error = getString(R.string.error_title_empty)
            valid = false
        }
        if (amount == null || amount <= 0) {
            binding.amountLayout.error = getString(R.string.error_amount)
            valid = false
        }
        if (categoryId == null) {
            binding.categoryLayout.error = getString(R.string.error_category)
            valid = false
        }
        if (!valid || amount == null || categoryId == null) {
            Snackbar.make(binding.root, R.string.error_fix_fields, Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.save(
            id = editId,
            title = title,
            description = binding.editDescription.text.toString().trim(),
            amountMinor = amount,
            categoryId = categoryId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}