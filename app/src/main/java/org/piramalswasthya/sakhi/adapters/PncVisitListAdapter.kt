package org.piramalswasthya.sakhi.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPncVisitBinding
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

class PncVisitListAdapter(private val clickListener: PncVisitClickListener? = null) :
    ListAdapter<BenPncDomain, PncVisitListAdapter.PregnancyVisitViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenPncDomain>() {
        override fun areItemsTheSame(
            oldItem: BenPncDomain, newItem: BenPncDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenPncDomain, newItem: BenPncDomain
        ) = oldItem == newItem

    }

    class PregnancyVisitViewHolder private constructor(private val binding: RvItemPncVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): PregnancyVisitViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPncVisitBinding.inflate(layoutInflater, parent, false)
                return PregnancyVisitViewHolder(binding)
            }
        }

        fun bind(
            item: BenPncDomain, clickListener: PncVisitClickListener?
        ) {

            if (item.pncDate == 0L) {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails3.visibility = View.GONE
            } else if (item.pncDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
                item.pncDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)) {
                binding.ivFollowState.visibility = View.VISIBLE
                binding.llBenPwrTrackingDetails3.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.pncDate)
            } else {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails3.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.pncDate)
            }

            binding.visit = item
            binding.btnViewVisits.visibility =
                if (item.savedPncRecords.isEmpty()) View.INVISIBLE else View.VISIBLE
            binding.btnAddPnc.visibility =
                if (item.allowFill) View.VISIBLE else View.INVISIBLE
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = PregnancyVisitViewHolder.from(parent)

    override fun onBindViewHolder(holder: PregnancyVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class PncVisitClickListener(
        private val showVisits: (benId: Long) -> Unit,
        private val addVisit: (benId: Long, hhId: Long ,visitNumber: Int) -> Unit,

        ) {
        fun showVisits(item: BenPncDomain) = showVisits(
            item.ben.benId,

        )

        fun addVisit(item: BenPncDomain) = addVisit(item.ben.benId,item.ben.hhId,
            if (item.savedPncRecords.isEmpty()) 1 else item.savedPncRecords.maxOf { it.pncPeriod } + 1)
    }

}