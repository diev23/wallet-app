package com.Gumbatov.wallet.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Gumbatov.wallet.databinding.ItemManageRowBinding
import com.Gumbatov.wallet.models.ManageRow

/** Shows rows of the categories / tags lists. Click = edit, button = delete. */
class ManageRowAdapter(
    private val onClick: (ManageRow) -> Unit,
    private val onDelete: (ManageRow) -> Unit
) : ListAdapter<ManageRow, ManageRowAdapter.ViewHolder>(ManageRowDiff) {

    class ViewHolder(val binding: ItemManageRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemManageRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = getItem(position)
        with(holder.binding) {
            rowTitle.text = row.title
            rowSubtitle.text = row.subtitle
            val color = row.colorHex
            rowColor.visibility = if (color == null) View.GONE else View.VISIBLE
            if (color != null) {
                rowColor.setBackgroundColor(
                    runCatching { Color.parseColor(color) }.getOrDefault(Color.GRAY)
                )
            }
            root.setOnClickListener { onClick(row) }
            rowDelete.setOnClickListener { onDelete(row) }
        }
    }
}

private object ManageRowDiff : DiffUtil.ItemCallback<ManageRow>() {
    override fun areItemsTheSame(oldItem: ManageRow, newItem: ManageRow) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ManageRow, newItem: ManageRow) = oldItem == newItem
}