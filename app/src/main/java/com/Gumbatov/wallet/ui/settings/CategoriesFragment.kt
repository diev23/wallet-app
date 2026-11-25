package com.Gumbatov.wallet.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.adapters.ManageRowAdapter
import com.Gumbatov.wallet.databinding.DialogCategoryBinding
import com.Gumbatov.wallet.databinding.FragmentManageListBinding
import com.Gumbatov.wallet.models.Category
import com.Gumbatov.wallet.models.ManageRow
import com.Gumbatov.wallet.viewmodels.CategoriesViewModel
import com.Gumbatov.wallet.viewmodels.CategoryRow
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** Create, edit and delete categories. */
class CategoriesFragment : Fragment(R.layout.fragment_manage_list) {

    private var _binding: FragmentManageListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels()
    private var rows: List<CategoryRow> = emptyList()

    /** Colors the user can choose: name of the color and its hex code. */
    private val palette = listOf(
        R.string.color_red to "#E53935",
        R.string.color_orange to "#FB8C00",
        R.string.color_yellow to "#FDD835",
        R.string.color_green to "#43A047",
        R.string.color_teal to "#00897B",
        R.string.color_blue to "#1E88E5",
        R.string.color_purple to "#8E24AA",
        R.string.color_gray to "#757575"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageListBinding.bind(view)

        val adapter = ManageRowAdapter(
            onClick = { row -> findRow(row)?.let { showEditDialog(it.category) } },
            onDelete = { row -> findRow(row)?.let { confirmDelete(it) } }
        )
        binding.manageList.layoutManager = LinearLayoutManager(requireContext())
        binding.manageList.adapter = adapter
        binding.manageEmpty.setText(R.string.manage_categories_empty)
        binding.manageAdd.setOnClickListener { showEditDialog(null) }

        viewModel.rows.observe(viewLifecycleOwner) { list ->
            rows = list
            adapter.submitList(list.map { toManageRow(it) })
            binding.manageEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.errorShown()
            }
        }
    }

    private fun findRow(row: ManageRow): CategoryRow? = rows.firstOrNull { it.category.id == row.id }

    private fun toManageRow(row: CategoryRow): ManageRow {
        val typeLabel = getString(if (row.category.isIncome) R.string.type_income else R.string.type_expense)
        return ManageRow(
            id = row.category.id,
            title = row.category.name,
            subtitle = getString(R.string.category_subtitle, typeLabel, row.transactionCount),
            colorHex = row.category.color
        )
    }

    /** One dialog for both cases: existing == null means a new category. */
    private fun showEditDialog(existing: Category?) {
        val dialogBinding = DialogCategoryBinding.inflate(layoutInflater)
        var selectedHex = existing?.color ?: palette.first().second

        fun showColor() {
            dialogBinding.colorPreview.setBackgroundColor(
                runCatching { Color.parseColor(selectedHex) }.getOrDefault(Color.GRAY)
            )
            val colorName = palette.firstOrNull { it.second.equals(selectedHex, ignoreCase = true) }
                ?.let { getString(it.first) } ?: selectedHex
            dialogBinding.colorInput.setText(colorName, false)
        }

        dialogBinding.colorInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                palette.map { getString(it.first) }
            )
        )
        dialogBinding.colorInput.setOnItemClickListener { _, _, position, _ ->
            selectedHex = palette[position].second
            showColor()
        }
        dialogBinding.nameInput.setText(existing?.name.orEmpty())
        dialogBinding.categoryTypeToggle.check(
            if (existing?.isIncome == true) R.id.category_type_income else R.id.category_type_expense
        )
        showColor()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.category_new else R.string.category_edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null) // replaced below: no auto-close on error
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.nameInput.text.toString().trim()
            if (name.isEmpty()) {
                dialogBinding.nameLayout.error = getString(R.string.error_name_empty)
                return@setOnClickListener
            }
            val isIncome = dialogBinding.categoryTypeToggle.checkedButtonId == R.id.category_type_income
            viewModel.save(Category(id = existing?.id ?: 0L, name = name, color = selectedHex, isIncome = isIncome))
            dialog.dismiss()
        }
    }

    /** Warns about the cascade: the records of the category are deleted too. */
    private fun confirmDelete(row: CategoryRow) {
        val message = if (row.transactionCount > 0) {
            getString(R.string.delete_category_with_records, row.category.name, row.transactionCount)
        } else {
            getString(R.string.delete_category_empty, row.category.name)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_category_title)
            .setMessage(message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete(row.category) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}