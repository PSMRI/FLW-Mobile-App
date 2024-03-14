package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPncBottomsheetBinding
import org.piramalswasthya.sakhi.model.PncDomain

class PncVisitAdapter(private val clickListener: PncVisitClickListener) :
    ListAdapter<PncDomain, PncVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<PncDomain>() {
        override fun areItemsTheSame(
            oldItem: PncDomain, newItem: PncDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: PncDomain, newItem: PncDomain
        ) = oldItem == newItem

    }

    class AncViewHolder private constructor(private val binding: RvItemPncBottomsheetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AncViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPncBottomsheetBinding.inflate(layoutInflater, parent, false)
                return AncViewHolder(binding)
            }
        }

        fun bind(
            item: PncDomain, clickListener: PncVisitClickListener
        ) {
            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = AncViewHolder.from(parent)

    override fun onBindViewHolder(holder: AncViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class PncVisitClickListener(
        private val clickedForm: (benId: Long, visitNumber: Int) -> Unit,

        ) {
        fun onClickedVisit(item: PncDomain) = clickedForm(
            item.benId, item.visitNumber
        )
    }

}