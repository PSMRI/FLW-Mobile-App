package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemEcTrackingListBinding
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

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

            if (item.savedECTRecords.isEmpty()) {
                binding.llEcTrackingDetails3.visibility = View.GONE
            } else {
                binding.llEcTrackingDetails3.visibility = View.VISIBLE
            }

            if (item.ectDate == 0L) {
                binding.ivFollowState.visibility = View.GONE
                binding.llVisitDate.visibility = View.INVISIBLE
            } else if (item.ectDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
                item.ectDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)) {
                binding.ivFollowState.visibility = View.VISIBLE
                binding.llVisitDate.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ectDate)
            } else {
                binding.ivFollowState.visibility = View.GONE
                binding.llVisitDate.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ectDate)
            }

            if (item.lmpDate != 0L) {
                binding.benLmpDate.text = HelperUtil.getDateStringFromLongStraight(item.lmpDate)
                if (System.currentTimeMillis() - item.lmpDate > TimeUnit.DAYS.toMillis(35)) {
                    binding.ivMissState.visibility = View.VISIBLE
                    binding.benStatus.text = "Missed Period"
                } else {
                    binding.ivMissState.visibility = View.GONE
                    binding.benStatus.text = "Under Review"
                }
            } else {
                binding.ivMissState.visibility = View.GONE
                binding.llEcTrackingDetails3.visibility = View.GONE
            }

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
        private val addNewTrack: (benId: Long, isAddEnabled: Boolean) -> Unit,
        private val showAllTracks: (benId: Long) -> Unit,

        ) {
        fun onClickedAdd(item: BenWithEctListDomain) = addNewTrack(
            item.ben.benId,
            item.allowFill
        )

        fun onClickedShowAllTracks(item: BenWithEctListDomain) = showAllTracks(
            item.ben.benId
        )
    }

}