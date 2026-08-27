package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI
import java.text.NumberFormat
import java.util.*

class ActivityAdapter(
    private val onClick: ((ClaimedIncentiveUI) -> Unit)? = null
) : ListAdapter<ClaimedIncentiveUI, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val layout = when (viewType) {
            TYPE_MITANIN -> R.layout.mitanin_item_layout
            else -> R.layout.item_activity
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1, onClick)
    }

    companion object {
        private const val TYPE_DEFAULT = 0
        private const val TYPE_MITANIN = 1
    }
    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        private val tvActivityAmount: TextView = itemView.findViewById(R.id.tvActivityAmount)
        private val tvActivityDesc: TextView = itemView.findViewById(R.id.tvActivityDesc)
        private val tvClaimCount: TextView = itemView.findViewById(R.id.tvClaimCount)
        private val tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        private val clMain: View? = itemView.findViewById(R.id.clMain)
        private val ClmainTwo: View? = itemView.findViewById(R.id.ClmainTwo)
        private val tvAmount: TextView? = itemView.findViewById(R.id.tvAmount)
        private val layoutContent: View? = itemView.findViewById(R.id.layoutContent)
        private val layoutApproval: View? = itemView.findViewById(R.id.layoutApproval)

        fun bind(item: ClaimedIncentiveUI, serialNo: Int, onClick: ((ClaimedIncentiveUI) -> Unit)?) {
            tvSerialNo.text = serialNo.toString()
            tvActivityName.text = item.groupName ?: "Activity : ${item.activityId}"
            tvActivityDesc.text = item.activityDec ?: ""
            tvClaimCount.text = item.claimCount.toString()
            tvActivityAmount.text = formatAmount(item.totalAmount)
            tvAmount?.text = formatAmount(item.totalAmount)
            if (item.approvalStatus == 105 && item.isDefault) {
                layoutApproval?.visibility = View.VISIBLE
            } else {
                layoutApproval?.visibility = View.GONE

            }
            if (item.isDefault) {
                clMain?.setBackgroundColor(
                    itemView.context.getColor(R.color.default_incentive_no_ben_background)
                )
                layoutContent?.setBackgroundColor(
                    itemView.context.getColor(R.color.unclaimed_pink)
                )
                clMain?.setOnClickListener(null)
                ClmainTwo?.setOnClickListener(null)
            } else {
                clMain?.setBackgroundColor(itemView.context.getColor(android.R.color.white))
                layoutContent?.setBackgroundColor(
                    itemView.context.getColor(android.R.color.white)
                )
                clMain?.setOnClickListener { onClick?.invoke(item) }
                ClmainTwo?.setOnClickListener{onClick?.invoke(item)}
            }

            if (item.isDefaultActivity) {
                clMain?.setBackgroundColor(
                    itemView.context.getColor(R.color.default_incentive_no_ben_background)
                )
                clMain?.setOnClickListener(null)
                ClmainTwo?.setOnClickListener(null)
            } else {
                clMain?.setBackgroundColor(itemView.context.getColor(android.R.color.white))
                clMain?.setOnClickListener { onClick?.invoke(item) }
                ClmainTwo?.setOnClickListener{onClick?.invoke(item)}

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

    override fun getItemViewType(position: Int): Int {
        return if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            TYPE_DEFAULT// TYPE_MITANIN
        } else {
            TYPE_DEFAULT
        }
    }
}