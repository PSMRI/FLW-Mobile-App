package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyVisitBinding
import org.piramalswasthya.sakhi.model.PregnantWomenVisitDomain

class AncVisitListAdapter(private val clickListener: PregnancyVisitClickListener? = null) :
    ListAdapter<PregnantWomenVisitDomain, AncVisitListAdapter.PregnancyVisitViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<PregnantWomenVisitDomain>() {
        override fun areItemsTheSame(
            oldItem: PregnantWomenVisitDomain, newItem: PregnantWomenVisitDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: PregnantWomenVisitDomain, newItem: PregnantWomenVisitDomain
        ) = oldItem == newItem

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
            item: PregnantWomenVisitDomain, clickListener: PregnancyVisitClickListener?
        ) {
            binding.visit = item
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


    class PregnancyVisitClickListener(
        private val clickedForm: (benId: Long) -> Unit,

        ) {
        fun onClickedVisit(item: PregnantWomenVisitDomain) = clickedForm(
            item.benId,
        )
    }

}