package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyVisitBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

private fun View.setVisibleIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.INVISIBLE
}

class AncVisitListAdapter(
    private val clickListener: PregnancyVisitClickListener? = null,
    private val showCall: Boolean = false,
    private val pref: PreferenceDao? = null,
    private val isHighRiskMode: Boolean = false
) : ListAdapter<BenWithAncListDomain, AncVisitListAdapter.PregnancyVisitViewHolder>(
    MyDiffUtilCallBack
) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenWithAncListDomain>() {
        override fun areItemsTheSame(oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain) =
            oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain) =
            oldItem == newItem
    }

    class PregnancyVisitViewHolder private constructor(private val binding: RvItemPregnancyVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PregnancyVisitViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPregnancyVisitBinding.inflate(layoutInflater, parent, false)
                return PregnancyVisitViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithAncListDomain,
            clickListener: PregnancyVisitClickListener?,
            showCall: Boolean,
            pref: PreferenceDao?,
            isHighRiskMode: Boolean
        ) {

            if (pref?.getLoggedInUser()?.role.equals("asha", true)) {
                binding.btnPmsma.visibility = View.VISIBLE
                binding.btnAddAnc.visibility = View.VISIBLE
            } else {
                binding.btnPmsma.visibility = View.INVISIBLE
                binding.btnAddAnc.visibility = View.INVISIBLE
            }

            if (item.ancDate == 0L) {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails4.visibility = View.GONE
            } else if (item.ancDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
                item.ancDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)) {
                binding.ivFollowState.visibility = View.VISIBLE
                binding.llBenPwrTrackingDetails4.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ancDate)
            } else {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails4.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ancDate)
            }

            if (showCall) {
                binding.ivCall.visibility = View.VISIBLE
            } else {
                binding.ivCall.visibility = View.GONE
            }

            binding.visit = item
            binding.llAnc.visibility=if(!isHighRiskMode) View.VISIBLE else View.GONE
            binding.btnAddAnc.setVisibleIf(item.showAddAnc)
            val lastPmsmaVisitNumber = item.pmsma.maxOfOrNull { it.visitNumber } ?: 0
            val filledWeek = item.pmsma.maxOfOrNull { it.filledWeek } ?: 0
            binding.btnPmsma.setVisibleIf(item.pmsmaFillable && lastPmsmaVisitNumber < 4 && filledWeek == 1)
            binding.btnPmsma.text = "Add PMSMA"
            binding.btnViewVisitsPmsma.setVisibleIf(item.hasPmsma && lastPmsmaVisitNumber > 0)
            binding.btnPmsma.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_red_dark))
            binding.btnViewVisits.setVisibleIf(item.anc.isNotEmpty())

            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PregnancyVisitViewHolder.from(parent)

    override fun onBindViewHolder(holder: PregnancyVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, showCall, pref, isHighRiskMode)
    }

    class PregnancyVisitClickListener(
        private val showVisits: (benId: Long) -> Unit,
        private val showPmsmaVisits: (benId: Long, hhId: Long) -> Unit,
        private val addVisit: (benId: Long, visitNumber: Int) -> Unit,
        private val pmsma: (benId: Long, hhId: Long, visitNumber: Int) -> Unit,
        private val callBen: (ben: BenWithAncListDomain) -> Unit
    ) {
        fun showVisits(item: BenWithAncListDomain) = showVisits(item.ben.benId)
        fun showPmsmaVisits(item: BenWithAncListDomain) = showPmsmaVisits(item.ben.benId, item.ben.hhId)
        fun addVisit(item: BenWithAncListDomain) = addVisit(
            item.ben.benId,
            if (item.anc.isEmpty()) 1 else item.anc.maxOf { it.visitNumber } + 1
        )

        fun pmsma(item: BenWithAncListDomain) = pmsma(
            item.ben.benId,
            item.ben.hhId,
            if (item.pmsma.isEmpty()) 1 else item.pmsma.maxOf { it.visitNumber } + 1
        )

        fun onClickedForCall(item: BenWithAncListDomain) = callBen(item)
    }
}
