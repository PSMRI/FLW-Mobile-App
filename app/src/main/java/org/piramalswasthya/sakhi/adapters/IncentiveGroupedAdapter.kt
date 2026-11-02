package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemIncentiveGroupedBinding
import org.piramalswasthya.sakhi.model.IncentiveGrouped

class IncentiveGroupedAdapter(
    private val onItemClick: (Long, String) -> Unit
) :
    ListAdapter<IncentiveGrouped, IncentiveGroupedAdapter.IncentiveGroupedViewHolder>(
        IncentiveGroupedDiffUtilCallBack
    ) {

    private object IncentiveGroupedDiffUtilCallBack : DiffUtil.ItemCallback<IncentiveGrouped>() {
        override fun areItemsTheSame(oldItem: IncentiveGrouped, newItem: IncentiveGrouped) =
            oldItem.activityName == newItem.activityName

        override fun areContentsTheSame(oldItem: IncentiveGrouped, newItem: IncentiveGrouped) =
            oldItem == newItem
    }

    class IncentiveGroupedViewHolder( private val binding: RvItemIncentiveGroupedBinding, private val onItemClick: (Long, String) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        private val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)


        fun bind(item: IncentiveGrouped, clickListener: (Long, String) -> Unit,serialNo : Int) {
            binding.item = item
            binding.serialNo = serialNo
            binding.clickListener = clickListener
            binding.isMitanin = isMitanin

            if (isMitanin) {
//                binding.guideline75.setGuidelinePercent(0.9f)
            }

            val (statusText, statusBg) = when (item.approvalStatus) {
                101 -> R.string.verified to R.drawable.bg_status_verified
                103 -> R.string.rejected to R.drawable.bg_status_rejected
                104 -> R.string.overdue to R.drawable.bg_status_rejected
                else -> R.string.pending to R.drawable.bg_status_pending
            }
            binding.tvApprovalStatus.text = itemView.context.getString(statusText)
            binding.tvApprovalStatus.setBackgroundResource(statusBg)

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncentiveGroupedViewHolder {
        val binding = RvItemIncentiveGroupedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return IncentiveGroupedViewHolder(binding,onItemClick)
    }

    override fun onBindViewHolder(holder: IncentiveGroupedViewHolder, position: Int) {
        holder.bind(getItem(position),onItemClick,position+ 1)
    }
}