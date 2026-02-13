package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemFollowUpHeaderBinding
import org.piramalswasthya.sakhi.databinding.ItemFollowUpVisitBinding

class FollowUpVisitAdapter : ListAdapter<FollowUpVisitAdapter.FollowUpVisitItem, RecyclerView.ViewHolder>(
    DiffCallback
) {

    sealed class FollowUpVisitItem {
        object Header : FollowUpVisitItem()
        data class VisitDate(val sno: String, val date: String) : FollowUpVisitItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FollowUpVisitItem.Header -> TYPE_HEADER
            is FollowUpVisitItem.VisitDate -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemFollowUpHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemFollowUpVisitBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                VisitDateViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is VisitDateViewHolder -> {
                val item = getItem(position) as FollowUpVisitItem.VisitDate
                holder.bind(item)
            }
        }
    }

    class HeaderViewHolder(private val binding: ItemFollowUpHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.executePendingBindings()
        }
    }

    class VisitDateViewHolder(private val binding: ItemFollowUpVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FollowUpVisitItem.VisitDate) {
            binding.sno = item.sno
            binding.visitDate = item.date
            binding.executePendingBindings()
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        object DiffCallback : DiffUtil.ItemCallback<FollowUpVisitItem>() {
            override fun areItemsTheSame(oldItem: FollowUpVisitItem, newItem: FollowUpVisitItem): Boolean {
                return when {
                    oldItem is FollowUpVisitItem.Header && newItem is FollowUpVisitItem.Header -> true
                    oldItem is FollowUpVisitItem.VisitDate && newItem is FollowUpVisitItem.VisitDate -> 
                        oldItem.sno == newItem.sno && oldItem.date == newItem.date
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: FollowUpVisitItem, newItem: FollowUpVisitItem): Boolean {
                return when {
                    oldItem is FollowUpVisitItem.Header && newItem is FollowUpVisitItem.Header -> true
                    oldItem is FollowUpVisitItem.VisitDate && newItem is FollowUpVisitItem.VisitDate -> 
                        oldItem.sno == newItem.sno && oldItem.date == newItem.date
                    else -> false
                }
            }
        }
    }
}