package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutAhdItemBinding
import org.piramalswasthya.sakhi.model.AHDCache

class AHDAdapter(
    private val clickListener: AHDClickListener? = null,
) : ListAdapter<AHDCache, AHDAdapter.AHDViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<AHDCache>() {
        override fun areItemsTheSame(
            oldItem: AHDCache,
            newItem: AHDCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: AHDCache,
            newItem: AHDCache
        ) = oldItem == newItem
    }

    class AHDViewHolder private constructor(private val binding: LayoutAhdItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AHDViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutAhdItemBinding.inflate(layoutInflater, parent, false)
                return AHDViewHolder(binding)
            }
        }

        fun bind(item: AHDCache, clickListener: AHDClickListener?) {
            binding.ahd = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AHDViewHolder {
        return AHDViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AHDViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class AHDClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) = clickListener(id)
    }
}