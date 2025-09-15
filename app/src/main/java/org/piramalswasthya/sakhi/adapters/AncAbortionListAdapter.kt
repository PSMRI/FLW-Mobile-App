package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemAbortionBinding
import org.piramalswasthya.sakhi.model.BenWithAncListDomain

class AncAbortionListAdapter(
    private val clickListener: AbortionListClickListener? = null
) : ListAdapter<BenWithAncListDomain, AncAbortionListAdapter.AbortionViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<BenWithAncListDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain
        ) = oldItem == newItem
    }

    class AbortionViewHolder private constructor(
        private val binding: RvItemAbortionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BenWithAncListDomain, clickListener: AbortionListClickListener?) {
            binding.visit = item
            binding.clickListener = clickListener

            binding.btnPmsma.text = if (item.isAbortionFormFilled) "View" else "Add"
            binding.btnPmsma.setBackgroundColor(
                binding.root.resources.getColor(
                    if (item.isAbortionFormFilled) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            binding.btnViewVisits.visibility =View.INVISIBLE
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AbortionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemAbortionBinding.inflate(layoutInflater, parent, false)
                return AbortionViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AbortionViewHolder.from(parent)

    override fun onBindViewHolder(holder: AbortionViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class AbortionListClickListener(
        private val showVisits: (benId: Long) -> Unit,
        private val addVisit: (benId: Long) -> Unit,
    ) {
        fun showVisits(item: BenWithAncListDomain) = showVisits(item.ben.benId)
        fun addVisit(item: BenWithAncListDomain) = addVisit(item.ben.benId)
    }
}
