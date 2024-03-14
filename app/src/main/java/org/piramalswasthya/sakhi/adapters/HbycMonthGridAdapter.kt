package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemIconHbycBinding
import org.piramalswasthya.sakhi.model.HbycIcon

class HbycMonthGridAdapter(private val clickListener: HbycIconClickListener) :
    ListAdapter<HbycIcon, HbycMonthGridAdapter.IconViewHolder>(HbycIconDiffCallback) {
    object HbycIconDiffCallback : DiffUtil.ItemCallback<HbycIcon>() {
        override fun areItemsTheSame(oldItem: HbycIcon, newItem: HbycIcon) =
            oldItem.count == newItem.count

        override fun areContentsTheSame(oldItem: HbycIcon, newItem: HbycIcon) =
            (oldItem == newItem)

    }


    class IconViewHolder private constructor(private val binding: RvItemIconHbycBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): IconViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemIconHbycBinding.inflate(layoutInflater, parent, false)
                return IconViewHolder(binding)
            }
        }

        fun bind(item: HbycIcon, clickListener: HbycIconClickListener) {
            binding.hbycIcom = item
            binding.clickListener = clickListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (item.isFilled)
                    binding.cvIcon.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_green_dark,
                            binding.root.context.theme
                        )
                    )
                else
                    binding.cvIcon.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_light,
                            binding.root.context.theme
                        )
                    )
            } else
                if (item.isFilled)
                    binding.cvIcon.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_green_dark))
                else
                    binding.cvIcon.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_red_light))
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class HbycIconClickListener(val selectedListener: (NavDirections) -> Unit) {
        fun onClicked(item: HbycIcon) = selectedListener(item.destination)

    }
}