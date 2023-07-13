package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemHrpTrackBenBinding
import org.piramalswasthya.sakhi.model.HRPPregnantTrackDomain

class HRPPregTrackAdapter(
    private val clickListener: HRPTrackClickListener? = null,
    private val visit : String
) : ListAdapter<HRPPregnantTrackDomain, HRPPregTrackAdapter.BenHRPTrackViewHolder>(
    BenDiffUtilCallBack
) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<HRPPregnantTrackDomain>() {
        override fun areItemsTheSame(
            oldItem: HRPPregnantTrackDomain, newItem: HRPPregnantTrackDomain
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: HRPPregnantTrackDomain, newItem: HRPPregnantTrackDomain
        ) = oldItem == newItem

    }

    class BenHRPTrackViewHolder private constructor(private val binding: RvItemHrpTrackBenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenHRPTrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemHrpTrackBenBinding.inflate(layoutInflater, parent, false)
                return BenHRPTrackViewHolder(binding)
            }
        }

        fun bind(
            item: HRPPregnantTrackDomain, clickListener: HRPTrackClickListener?, visit: String
        ) {
            binding.track = item
            binding.clickListener = clickListener
            binding.visit = visit
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BenHRPTrackViewHolder = BenHRPTrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenHRPTrackViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, visit)
    }


    class HRPTrackClickListener(
        private val clickedTrack: (trackId: Int) -> Unit,

        ) {
        fun onClickedBen(item: HRPPregnantTrackDomain) = clickedTrack(
            item.id
        )

    }

}