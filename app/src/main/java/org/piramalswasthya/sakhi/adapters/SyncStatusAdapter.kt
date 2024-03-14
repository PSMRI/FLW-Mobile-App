package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemSyncStatusBinding
import org.piramalswasthya.sakhi.model.SyncStatusDomain

class SyncStatusAdapter :
    ListAdapter<SyncStatusDomain, SyncStatusAdapter.SyncStatusViewHolder>(
        SyncItemDiffCallback
    ) {
    object SyncItemDiffCallback : DiffUtil.ItemCallback<SyncStatusDomain>() {
        override fun areItemsTheSame(
            oldItem: SyncStatusDomain,
            newItem: SyncStatusDomain
        ) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: SyncStatusDomain,
            newItem: SyncStatusDomain
        ) =
            (oldItem == newItem)

    }


    class SyncStatusViewHolder private constructor(private val binding: RvItemSyncStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): SyncStatusViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemSyncStatusBinding.inflate(layoutInflater, parent, false)
                return SyncStatusViewHolder(binding)
            }
        }

        fun bind(item: SyncStatusDomain) {
            binding.sync = item
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SyncStatusViewHolder.from(parent)

    override fun onBindViewHolder(holder: SyncStatusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}