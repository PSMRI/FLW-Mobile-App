package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutMaameetingItemBinding
import org.piramalswasthya.sakhi.model.MaaMeetingEntity

class MaaMeetingAdapter(
    private val clickListener: MaaMeetingAdapterClickListener? = null,
) : ListAdapter<MaaMeetingEntity, MaaMeetingAdapter.MaaMeetingViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<MaaMeetingEntity>() {
        override fun areItemsTheSame(
            oldItem: MaaMeetingEntity,
            newItem: MaaMeetingEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MaaMeetingEntity,
            newItem: MaaMeetingEntity
        ) = oldItem == newItem
    }

    class MaaMeetingViewHolder private constructor(private val binding: LayoutMaameetingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): MaaMeetingViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutMaameetingItemBinding.inflate(layoutInflater, parent, false)
                return MaaMeetingViewHolder(binding)
            }
        }

        fun bind(item: MaaMeetingEntity, clickListener: MaaMeetingAdapterClickListener?) {
            binding.maaMeeting = item

            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaaMeetingViewHolder {
        return MaaMeetingViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MaaMeetingViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class MaaMeetingAdapterClickListener(val clickListener: (id: Long) -> Unit) {
        fun onClick(id: Long) = clickListener(id)
    }
}