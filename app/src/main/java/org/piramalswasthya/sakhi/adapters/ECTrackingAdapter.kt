package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemEcTrackBinding
import org.piramalswasthya.sakhi.model.ECTDomain

class ECTrackingAdapter(private val clickListener: ECTrackViewClickListener) :
    ListAdapter<ECTDomain, ECTrackingAdapter.ECTrackViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<ECTDomain>() {
        override fun areItemsTheSame(
            oldItem: ECTDomain, newItem: ECTDomain
        ) = oldItem.created == newItem.created

        override fun areContentsTheSame(
            oldItem: ECTDomain, newItem: ECTDomain
        ) = oldItem == newItem

    }

    class ECTrackViewHolder private constructor(private val binding: RvItemEcTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ECTrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemEcTrackBinding.inflate(layoutInflater, parent, false)
                return ECTrackViewHolder(binding)
            }
        }

        fun bind(
            item: ECTDomain, clickListener: ECTrackViewClickListener
        ) {
            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = ECTrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: ECTrackViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ECTrackViewClickListener(
        private val clickedForm: (benId: Long, created: Long) -> Unit,

        ) {
        fun onClickedVisit(item: ECTDomain) = clickedForm(
            item.benId, item.created
        )
    }

}