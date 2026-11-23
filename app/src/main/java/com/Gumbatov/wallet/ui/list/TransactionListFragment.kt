package com.Gumbatov.wallet.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.adapters.TransactionAdapter
import com.Gumbatov.wallet.databinding.FragmentTransactionListBinding
import com.Gumbatov.wallet.viewmodels.TransactionListViewModel
import com.google.android.material.snackbar.Snackbar

/** Main screen: list of transactions with an empty state and an add button. */
class TransactionListFragment : Fragment(R.layout.fragment_transaction_list) {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionListViewModel by viewModels()

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
        binding.emptyAddButton.setOnClickListener { onAddClicked() }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.progressBar.visibility = View.GONE
            adapter.submitList(items)
            val isEmpty = items.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // avoid leaking the views after the screen is destroyed
    }
}