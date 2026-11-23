package com.Gumbatov.wallet.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Gumbatov.wallet.R
import com.Gumbatov.wallet.databinding.ItemTransactionBinding
import com.Gumbatov.wallet.models.TransactionItem
import com.Gumbatov.wallet.utils.Formatters

/** Shows transactions in a RecyclerView; [onItemClick] is called when a row is tapped. */
class TransactionAdapter(
    private val onItemClick: (TransactionItem) -> Unit
) : ListAdapter<TransactionItem, TransactionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val amountColor = if (item.category.isIncome) R.color.income else R.color.expense

        with(holder.binding) {
            title.text = item.transaction.title
            subtitle.text = item.category.name + " · " +
                    Formatters.formatDate(item.transaction.date)
            amount.text = Formatters.formatAmount(
                item.transaction.amountMinor, item.category.isIncome
            )
            amount.setTextColor(ContextCompat.getColor(context, amountColor))
            categoryColor.setBackgroundColor(Color.parseColor(item.category.color))
            root.setOnClickListener { onItemClick(item) }
        }
    }

    /** Tells the adapter which rows are the same record and which have changed. */
    private object DiffCallback : DiffUtil.ItemCallback<TransactionItem>() {
        override fun areItemsTheSame(old: TransactionItem, new: TransactionItem) =
            old.transaction.id == new.transaction.id

        override fun areContentsTheSame(old: TransactionItem, new: TransactionItem) =
            old == new
    }
}