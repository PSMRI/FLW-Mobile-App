package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyAncBinding
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyAncPmsmaBinding
import org.piramalswasthya.sakhi.model.AncStatus

class PmsmaVisitAdapter(private val clickListener: PmsmaVisitClickListener) :
    ListAdapter<AncStatus, PmsmaVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<AncStatus>() {
        override fun areItemsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem == newItem

    }

    class AncViewHolder private constructor(private val binding: RvItemPregnancyAncPmsmaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AncViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPregnancyAncPmsmaBinding.inflate(layoutInflater, parent, false)
                return AncViewHolder(binding)
            }
        }

        fun bind(
            item: AncStatus, clickListener: PmsmaVisitClickListener, isLastItem: Boolean
        ) {
            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.btnView.setOnClickListener {
                clickListener.onClickedVisit(item, true)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = AncViewHolder.from(parent)

    override fun onBindViewHolder(holder: AncViewHolder, position: Int) {
        val isLastItem = position == itemCount - 1
        holder.bind(getItem(position), clickListener, isLastItem)
    }


    class PmsmaVisitClickListener(
        private val clickedForm: (benId: Long, visitNumber: Int, isLast: Boolean) -> Unit
    ) {
        fun onClickedVisit(item: AncStatus, isLast: Boolean) = clickedForm(
            item.benId, item.visitNumber, isLast
        )
    }

}