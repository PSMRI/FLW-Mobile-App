package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemSyncDashboardStatusBinding
import org.piramalswasthya.sakhi.model.SyncStatusDomain

class SyncDashboardStatusAdapter :
    ListAdapter<SyncStatusDomain, SyncDashboardStatusAdapter.ViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<SyncStatusDomain>() {
        override fun areItemsTheSame(oldItem: SyncStatusDomain, newItem: SyncStatusDomain) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: SyncStatusDomain, newItem: SyncStatusDomain) =
            oldItem == newItem
    }

    class ViewHolder(
        private val binding: RvItemSyncDashboardStatusBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncStatusDomain) {
            binding.tvEntityName.text = item.name
            binding.tvSynced.text = item.synced.toString()
            binding.tvUnsynced.text = item.notSynced.toString()
            binding.tvSyncing.text = item.syncing.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemSyncDashboardStatusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
