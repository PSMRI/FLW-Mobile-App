package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.ui.design.setDsSyncBadge
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
            // Redesign (Figma 08 · Sync row): "N records · M waiting" + sync badge
            val ctx = binding.root.context
            val waiting = item.notSynced + item.syncing
            binding.tvRecords.text = if (waiting == 0) ctx.getString(R.string.ds_sync_records, item.totalCount)
            else ctx.getString(R.string.ds_sync_records_waiting, item.totalCount, waiting)
            binding.tvStatusBadge.setDsSyncBadge(
                when {
                    item.syncing > 0 -> SyncState.SYNCING
                    item.notSynced > 0 -> SyncState.UNSYNCED
                    else -> SyncState.SYNCED
                }
            )
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
