package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.AshaWorker
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.VerificationStatus
import org.piramalswasthya.sakhi.R
import java.text.NumberFormat
import java.util.*

class AshaWorkerAdapter(
    private val onItemClick: (AshaWorker) -> Unit
) : ListAdapter<AshaWorker, AshaWorkerAdapter.AshaWorkerViewHolder>(AshaWorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AshaWorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asha_worker, parent, false)
        return AshaWorkerViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: AshaWorkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AshaWorkerViewHolder(
        itemView: View,
        private val onItemClick: (AshaWorker) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvWorkerName: TextView = itemView.findViewById(R.id.tvWorkerName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvAshaIdCenter: TextView = itemView.findViewById(R.id.tvAshaIdCenter)
        private val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        private val tvTotalIncentive: TextView = itemView.findViewById(R.id.tvTotalIncentive)

        fun bind(worker: AshaWorker) {
            tvWorkerName.text = worker.name
            tvAmount.text = formatAmount(worker.amount)
            tvAshaIdCenter.text = "${worker.ashaId} · ${worker.serviceCenter}"
            tvTotalIncentive.text = "Total Incentive: ${formatAmount(worker.totalIncentive)}"

            // Set status badge
            when (worker.status) {
                VerificationStatus.VERIFIED -> {
                    statusBadge.text = "Verified"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_verified)
                }
                VerificationStatus.PENDING -> {
                    statusBadge.text = "Pending"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                }
                VerificationStatus.REJECTED -> {
                    statusBadge.text = "Rejected"
                    statusBadge.setBackgroundResource(R.drawable.bg_status_rejected)
                }
            }

            itemView.setOnClickListener {
                onItemClick(worker)
            }
        }

        private fun formatAmount(amount: Int): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }

    class AshaWorkerDiffCallback : DiffUtil.ItemCallback<AshaWorker>() {
        override fun areItemsTheSame(oldItem: AshaWorker, newItem: AshaWorker): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AshaWorker, newItem: AshaWorker): Boolean {
            return oldItem == newItem
        }
    }
}
