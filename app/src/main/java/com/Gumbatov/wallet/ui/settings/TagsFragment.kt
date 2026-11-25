package com.Gumbatov.wallet.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.adapters.ManageRowAdapter
import com.Gumbatov.wallet.databinding.FragmentManageListBinding
import com.Gumbatov.wallet.models.ManageRow
import com.Gumbatov.wallet.viewmodels.TagRow
import com.Gumbatov.wallet.viewmodels.TagsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/** Add and delete tags. */
class TagsFragment : Fragment(R.layout.fragment_manage_list) {

    private var _binding: FragmentManageListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TagsViewModel by viewModels()
    private var rows: List<TagRow> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageListBinding.bind(view)

        val adapter = ManageRowAdapter(
            onClick = { }, // a tag has only a name, there is nothing to edit
            onDelete = { row -> rows.firstOrNull { it.tag.id == row.id }?.let { confirmDelete(it) } }
        )
        binding.manageList.layoutManager = LinearLayoutManager(requireContext())
        binding.manageList.adapter = adapter
        binding.manageEmpty.setText(R.string.manage_tags_empty)
        binding.manageAdd.setOnClickListener { showAddDialog() }

        viewModel.rows.observe(viewLifecycleOwner) { list ->
            rows = list
            adapter.submitList(
                list.map {
                    ManageRow(
                        id = it.tag.id,
                        title = it.tag.name,
                        subtitle = getString(R.string.tag_subtitle, it.usageCount),
                        colorHex = null
                    )
                }
            )
            binding.manageEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.errorShown()
            }
        }
    }

    private fun showAddDialog() {
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
                    viewModel.add(name)
                }
            }
            .show()
    }

    private fun confirmDelete(row: TagRow) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_tag_title)
            .setMessage(getString(R.string.delete_tag_message, row.tag.name, row.usageCount))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete(row.tag) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}