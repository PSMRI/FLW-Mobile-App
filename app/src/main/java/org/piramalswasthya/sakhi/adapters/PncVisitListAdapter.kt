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
        private val addVisit: (benId: Long, visitNumber: Int) -> Unit,

        ) {
        fun showVisits(item: BenPncDomain) = showVisits(
            item.ben.benId,
        )

        fun addVisit(item: BenPncDomain) = addVisit(item.ben.benId,
            if (item.savedPncRecords.isEmpty()) 1 else item.savedPncRecords.maxOf { it.pncPeriod } + 1)
    }

}