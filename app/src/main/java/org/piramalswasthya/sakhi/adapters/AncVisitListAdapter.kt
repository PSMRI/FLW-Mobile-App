package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyVisitBinding
import org.piramalswasthya.sakhi.model.BenWithAncListDomain

private fun View.setVisibleIf(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.INVISIBLE
}

class AncVisitListAdapter(
    private val clickListener: PregnancyVisitClickListener? = null,
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
            isHighRiskMode: Boolean
        ) {
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
        holder.bind(getItem(position), clickListener, isHighRiskMode)
    }

    class PregnancyVisitClickListener(
        private val showVisits: (benId: Long) -> Unit,
        private val showPmsmaVisits: (benId: Long, hhId: Long) -> Unit,
        private val addVisit: (benId: Long, visitNumber: Int) -> Unit,
        private val pmsma: (benId: Long, hhId: Long, visitNumber: Int) -> Unit,
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
    }
}
