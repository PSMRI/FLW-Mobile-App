package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI
import java.text.NumberFormat
import java.util.*

class ActivityAdapter(
    private val onClick: ((ClaimedIncentiveUI) -> Unit)? = null
) : ListAdapter<ClaimedIncentiveUI, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1, onClick)
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        private val tvActivityAmount: TextView = itemView.findViewById(R.id.tvActivityAmount)
        private val tvActivityDesc: TextView = itemView.findViewById(R.id.tvActivityDesc)
        private val tvClaimCount: TextView = itemView.findViewById(R.id.tvClaimCount)
        private val tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        private val clMain: View = itemView.findViewById(R.id.clMain)

        fun bind(item: ClaimedIncentiveUI, serialNo: Int, onClick: ((ClaimedIncentiveUI) -> Unit)?) {
            tvSerialNo.text = serialNo.toString()
            tvActivityName.text = item.groupName ?: "Activity : ${item.activityId}"
            tvActivityDesc.text = item.activityDec ?: ""
            tvClaimCount.text = item.claimCount.toString()
            tvActivityAmount.text = formatAmount(item.totalAmount)

            if (item.isDefaultActivity) {
                clMain.setBackgroundColor(
                    itemView.context.getColor(R.color.default_incentive_no_ben_background)
                )
                clMain.setOnClickListener(null)
            } else {
                clMain.setBackgroundColor(itemView.context.getColor(android.R.color.white))
                clMain.setOnClickListener { onClick?.invoke(item) }
            }

        }

        private fun formatAmount(amount: Int): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ClaimedIncentiveUI>() {
        override fun areItemsTheSame(oldItem: ClaimedIncentiveUI, newItem: ClaimedIncentiveUI) =
            oldItem.activityId == newItem.activityId

        override fun areContentsTheSame(oldItem: ClaimedIncentiveUI, newItem: ClaimedIncentiveUI) =
            oldItem == newItem
    }
}