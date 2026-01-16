package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutOrsCampaignItemBinding
import org.piramalswasthya.sakhi.model.ORSCampaignCache

class ORSCampaignAdapter(
    private val clickListener: ORSCampaignClickListener? = null,
) : ListAdapter<ORSCampaignCache, ORSCampaignAdapter.ORSCampaignViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<ORSCampaignCache>() {
        override fun areItemsTheSame(
            oldItem: ORSCampaignCache,
            newItem: ORSCampaignCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ORSCampaignCache,
            newItem: ORSCampaignCache
        ) = oldItem == newItem
    }

    class ORSCampaignViewHolder private constructor(private val binding: LayoutOrsCampaignItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ORSCampaignViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutOrsCampaignItemBinding.inflate(layoutInflater, parent, false)
                return ORSCampaignViewHolder(binding)
            }
        }

        fun bind(item: ORSCampaignCache, clickListener: ORSCampaignClickListener?) {
            binding.orsCampaign = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ORSCampaignViewHolder {
        return ORSCampaignViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ORSCampaignViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ORSCampaignClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) = clickListener(id)
    }
}
