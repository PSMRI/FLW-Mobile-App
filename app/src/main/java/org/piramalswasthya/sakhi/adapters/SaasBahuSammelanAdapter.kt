package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutSaasBahuListBinding
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache

class SaasBahuSammelanAdapter(
    private val clickListener: SaasBahuSammelanAdapterClickListener? = null,
) : ListAdapter<SaasBahuSammelanCache, SaasBahuSammelanAdapter.SaasBahuSammelanViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<SaasBahuSammelanCache>() {
        override fun areItemsTheSame(
            oldItem: SaasBahuSammelanCache,
            newItem: SaasBahuSammelanCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SaasBahuSammelanCache,
            newItem: SaasBahuSammelanCache
        ) = oldItem == newItem
    }

    class SaasBahuSammelanViewHolder private constructor(private val binding: LayoutSaasBahuListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): SaasBahuSammelanViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutSaasBahuListBinding.inflate(layoutInflater, parent, false)
                return SaasBahuSammelanViewHolder(binding)
            }
        }

        fun bind(item: SaasBahuSammelanCache, clickListener: SaasBahuSammelanAdapterClickListener?) {
            binding.maaMeeting = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaasBahuSammelanViewHolder {
        return SaasBahuSammelanViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SaasBahuSammelanViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class SaasBahuSammelanAdapterClickListener(val clickListener: (id: Long) -> Unit) {
        fun onClick(id: Long) = clickListener(id)
    }
}