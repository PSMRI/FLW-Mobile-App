package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyVisitBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

private fun View.setVisibleIf(condition: Boolean) {
    this.isEnabled = condition
    this.alpha = if (condition) 1.0f else 0.5f
}
private fun View.setVisibleIfViewVisit(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.INVISIBLE
}

class AncVisitListAdapter(
    private val clickListener: PregnancyVisitClickListener? = null,
    private val showCall: Boolean = false,
    private val pref: PreferenceDao? = null,
    private val isHighRiskMode: Boolean = false,
    private val hidePmsma: Boolean
) : ListAdapter<BenWithAncListDomain, AncVisitListAdapter.PregnancyVisitViewHolder>(
    MyDiffUtilCallBack
) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenWithAncListDomain>() {
        override fun areItemsTheSame(oldItem: BenWithAncListDomain, newItem: BenWithAncListDomain) =
            oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithAncListDomain,
            newItem: BenWithAncListDomain
        ) =
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
            isHighRiskMode: Boolean,
            hidePmsma: Boolean
        ) {

            if (pref?.getLoggedInUser()?.role.equals("asha", true)) {
                binding.btnAddAnc.visibility = View.VISIBLE
                binding.btnAddHomeVisit.visibility = View.VISIBLE
            } else {
                binding.btnAddAnc.visibility = View.INVISIBLE
            }

            if (item.ancDate == 0L) {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails4.visibility = View.GONE
            } else if (item.ancDate < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90) &&
                item.ancDate > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
            ) {
                binding.ivFollowState.visibility = View.VISIBLE
                binding.llBenPwrTrackingDetails4.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ancDate)
            } else {
                binding.ivFollowState.visibility = View.GONE
                binding.llBenPwrTrackingDetails4.visibility = View.VISIBLE
                binding.benVisitDate.text = HelperUtil.getDateStringFromLongStraight(item.ancDate)
            }

            binding.btnAddHomeVisit.visibility =
                if (item.showAddHomeVisit) View.VISIBLE else View.GONE

            binding.btnViewHomeVisit.visibility =
                if (item.showViewHomeVisit) View.VISIBLE else View.GONE

            if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true))
            {
                binding.btnAddHomeVisit.visibility = View.GONE
                binding.btnViewHomeVisit.visibility = View.GONE


            }

            if (showCall) {
                binding.ivCall.visibility = View.VISIBLE
            } else {
                binding.ivCall.visibility = View.GONE
            }

            binding.visit = item
            binding.llAnc.visibility = if (!isHighRiskMode) View.VISIBLE else View.GONE
            binding.btnAddAnc.setVisibleIf(item.showAddAnc)
            binding.btnPmsma.setVisibleIf(item.showAddAnc)
            binding.btnViewVisits.setVisibleIfViewVisit(item.anc.isNotEmpty())
            binding.btnViewVisitsPmsma.setVisibleIfViewVisit(item.anc.isNotEmpty())
            if (hidePmsma) {
                binding.btnPmsma.visibility = View.GONE
                binding.btnViewVisitsPmsma.visibility = View.GONE
            }
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PregnancyVisitViewHolder.from(parent)

    override fun onBindViewHolder(holder: PregnancyVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, showCall, pref, isHighRiskMode, hidePmsma)
    }

    class PregnancyVisitClickListener(
        private val showVisits: (benId: Long) -> Unit,
        private val showPmsmaVisits: (benId: Long, hhId: Long) -> Unit,
        private val addVisit: (benId: Long, hhId: Long, visitNumber: Int) -> Unit,
        private val pmsma: (benId: Long, hhId: Long, visitNumber: Int) -> Unit,
        private val callBen: (ben: BenWithAncListDomain) -> Unit,
        private val addHomeVisit: ((benId: Long) -> Unit)? = null,
        private val showHomeVisit : ((benId: Long) -> Unit)? = null
    ) {
        fun showVisits(item: BenWithAncListDomain) = showVisits(item.ben.benId)
        fun showPmsmaVisits(item: BenWithAncListDomain) =
            showPmsmaVisits(item.ben.benId, item.ben.hhId)

        fun addVisit(item: BenWithAncListDomain) = addVisit(
            item.ben.benId,
            item.ben.hhId,
            if (item.anc.isEmpty()) 1 else item.anc.maxOf { it.visitNumber } + 1
        )

        fun pmsma(item: BenWithAncListDomain) = pmsma(
            item.ben.benId,
            item.ben.hhId,
            if (item.pmsma.isEmpty()) 1 else item.pmsma.maxOf { it.visitNumber } + 1
        )

        fun onClickedForCall(item: BenWithAncListDomain) = callBen(item)
        fun addHomeVisit(item: BenWithAncListDomain) {
            addHomeVisit?.invoke(
                item.ben.benId,

            )
        }
        fun showHomeVisit(item: BenWithAncListDomain) = showHomeVisit?.invoke(item.ben.benId)

    }
}
