package com.Gumbatov.wallet.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.databinding.FragmentTransactionDetailBinding
import com.Gumbatov.wallet.utils.Formatters
import com.Gumbatov.wallet.viewmodels.TransactionDetailViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/** Read-only view of one transaction with Edit and Delete buttons. */
class TransactionDetailFragment : Fragment(R.layout.fragment_transaction_detail) {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionDetailViewModel by viewModels()
    private var transactionId = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransactionDetailBinding.bind(view)

        transactionId = arguments?.getLong("transactionId", 0L) ?: 0L
        viewModel.setId(transactionId)

        viewModel.item.observe(viewLifecycleOwner) { render() }
        viewModel.categories.observe(viewLifecycleOwner) { render() }
        viewModel.deleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) findNavController().popBackStack()
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.errorShown()
            }
        }

        binding.buttonEdit.setOnClickListener {
            findNavController().navigate(
                R.id.action_detail_to_edit,
                bundleOf("transactionId" to transactionId)
            )
        }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    /** Draws the screen once both the transaction and its category are known. */
    private fun render() {
        val item = viewModel.item.value ?: return
        val category = viewModel.categories.value
            ?.firstOrNull { it.id == item.transaction.categoryId } ?: return

        binding.detailTitle.text = item.transaction.title
        binding.detailAmount.text =
            Formatters.formatAmount(item.transaction.amountMinor, category.isIncome)
        binding.detailAmount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (category.isIncome) R.color.income else R.color.expense
            )
        )
        binding.detailCategory.text = category.name
        binding.detailCategoryColor.setBackgroundColor(
            runCatching { Color.parseColor(category.color) }.getOrDefault(Color.GRAY)
        )
        binding.detailDate.text = Formatters.formatDate(item.transaction.date)
        binding.detailDescription.text =
            item.transaction.description.ifBlank { getString(R.string.no_description) }

        binding.detailTags.removeAllViews()
        if (item.tags.isEmpty()) {
            binding.detailTags.addView(Chip(requireContext()).apply { setText(R.string.no_tags) })
        } else {
            item.tags.forEach { tag ->
                binding.detailTags.addView(Chip(requireContext()).apply { text = tag.name })
            }
        }
    }

    private fun confirmDelete() {
        val transaction = viewModel.item.value?.transaction ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete(transaction) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}