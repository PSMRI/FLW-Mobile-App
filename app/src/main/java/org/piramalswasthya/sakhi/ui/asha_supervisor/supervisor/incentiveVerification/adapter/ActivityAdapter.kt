package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityDetail
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityStatus
import java.text.NumberFormat
import java.util.*

class ActivityAdapter : ListAdapter<ActivityDetail, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        private val tvActivityAmount: TextView = itemView.findViewById(R.id.tvActivityAmount)
        private val tvActivityDate: TextView = itemView.findViewById(R.id.tvActivityDate)
        private val tvSubmittedOn: TextView = itemView.findViewById(R.id.tvSubmittedOn)
        private val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        private val tvStatusMessage: TextView = itemView.findViewById(R.id.tvStatusMessage)

        fun bind(activity: ActivityDetail) {
            tvActivityName.text = activity.name
            tvActivityAmount.text = formatAmount(activity.amount)
            tvActivityDate.text = "Activity Date: ${activity.activityDate}"
            tvSubmittedOn.text = "Submitted On: ${activity.submittedOn}"
            tvStatusMessage.text = activity.statusMessage

            // Set status badge
            when (activity.status) {
                ActivityStatus.VERIFIED -> {
                    statusBadge.text = "Verified"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_verified)
                }
                ActivityStatus.PENDING -> {
                    statusBadge.text = "Pending"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                }
                ActivityStatus.REJECTED -> {
                    statusBadge.text = "Rejected"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_rejected)
                }
            }
        }

        private fun formatAmount(amount: Int): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityDetail>() {
        override fun areItemsTheSame(oldItem: ActivityDetail, newItem: ActivityDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityDetail, newItem: ActivityDetail): Boolean {
            return oldItem == newItem
        }
    }
}
