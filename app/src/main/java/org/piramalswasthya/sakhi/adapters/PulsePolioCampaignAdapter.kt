package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutPulsePolioCampaignItemBinding
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache

class PulsePolioCampaignAdapter(
    private val clickListener: PulsePolioCampaignClickListener? = null,
) : ListAdapter<PulsePolioCampaignCache, PulsePolioCampaignAdapter.PulsePolioCampaignViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PulsePolioCampaignCache>() {
        override fun areItemsTheSame(
            oldItem: PulsePolioCampaignCache,
            newItem: PulsePolioCampaignCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PulsePolioCampaignCache,
            newItem: PulsePolioCampaignCache
        ) = oldItem == newItem
    }

    class PulsePolioCampaignViewHolder private constructor(private val binding: LayoutPulsePolioCampaignItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): PulsePolioCampaignViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutPulsePolioCampaignItemBinding.inflate(layoutInflater, parent, false)
                return PulsePolioCampaignViewHolder(binding)
            }
        }

        fun bind(item: PulsePolioCampaignCache, clickListener: PulsePolioCampaignClickListener?) {
            binding.pulsePolioCampaign = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PulsePolioCampaignViewHolder {
        return PulsePolioCampaignViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PulsePolioCampaignViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class PulsePolioCampaignClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) = clickListener(id)
    }
}
