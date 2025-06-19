package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.databinding.LayoutDewormingItemBinding

class DewormingAdapter(
    private val clickListener: DewormingClickListener? = null,
) : ListAdapter<DewormingCache, DewormingAdapter.DewormingViewHolder>(DewormingDiffUtilCallBack) {

    private object DewormingDiffUtilCallBack : DiffUtil.ItemCallback<DewormingCache>() {
        override fun areItemsTheSame(
            oldItem: DewormingCache,
            newItem: DewormingCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DewormingCache,
            newItem: DewormingCache
        ) = oldItem == newItem
    }

    class DewormingViewHolder private constructor(private val binding: LayoutDewormingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): DewormingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutDewormingItemBinding.inflate(layoutInflater, parent, false)
                return DewormingViewHolder(binding)
            }
        }

        fun bind(item: DewormingCache, clickListener: DewormingClickListener?) {
            binding.deworming = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DewormingViewHolder {
        return DewormingViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DewormingViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class DewormingClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) = clickListener(id)
    }
}