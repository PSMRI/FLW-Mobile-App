package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemEcTrackingListBinding
import org.piramalswasthya.sakhi.model.BenWithEctListDomain

class ECTrackingListAdapter(private val clickListener: ECTrackListClickListener) :
    ListAdapter<BenWithEctListDomain, ECTrackingListAdapter.ECTrackViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenWithEctListDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithEctListDomain, newItem: BenWithEctListDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithEctListDomain, newItem: BenWithEctListDomain
        ) = oldItem == newItem

    }

    class ECTrackViewHolder private constructor(private val binding: RvItemEcTrackingListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ECTrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemEcTrackingListBinding.inflate(layoutInflater, parent, false)
                return ECTrackViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithEctListDomain, clickListener: ECTrackListClickListener
        ) {
            binding.item = item
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


    class ECTrackListClickListener(
        private val addNewTrack: (benId: Long) -> Unit,
        private val showAllTracks: (benId: Long) -> Unit,

        ) {
        fun onClickedAdd(item : BenWithEctListDomain) = addNewTrack(
            item.ben.benId
        )

        fun onClickedShowAllTracks(item : BenWithEctListDomain) = showAllTracks(
            item.ben.benId
        )
    }

}