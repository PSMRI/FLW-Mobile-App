package org.piramalswasthya.sakhi.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemUwinListBinding
import org.piramalswasthya.sakhi.model.UwinCache

class UwinListAdapter(
    private val clickListener: UwinClickListener? = null
) : ListAdapter<UwinCache, UwinListAdapter.UwinViewHolder>(UwinDiffUtilCallback) {

    private object UwinDiffUtilCallback : DiffUtil.ItemCallback<UwinCache>() {
        override fun areItemsTheSame(oldItem: UwinCache, newItem: UwinCache): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UwinCache, newItem: UwinCache): Boolean =
            oldItem == newItem
    }

    class UwinViewHolder private constructor(private val binding: ItemUwinListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UwinCache, clickListener: UwinClickListener?) {
            binding.uwin = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): UwinViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemUwinListBinding.inflate(layoutInflater, parent, false)
                return UwinViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UwinViewHolder {
        return UwinViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: UwinViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class UwinClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) = clickListener(id)
    }
}