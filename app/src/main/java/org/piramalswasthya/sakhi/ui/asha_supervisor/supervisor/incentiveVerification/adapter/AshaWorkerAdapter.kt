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
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.AshaWorker
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.VerificationStatus
import org.piramalswasthya.sakhi.utils.HelperUtil
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
        private val tvStatusDate: TextView = itemView.findViewById(R.id.tvStatusDate)
        private val tvRejectedReason: TextView = itemView.findViewById(R.id.tvRejectedReason)
        private val tvStatusBy: TextView = itemView.findViewById(R.id.tvStatusBy)
        private val textShort: TextView = itemView.findViewById(R.id.textShort)

        fun getInitials(name: String): String {
            return name.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
        }
        fun bind(worker: AshaWorker) {
            tvWorkerName.text = worker.name
            textShort.text = getInitials(tvWorkerName.text.toString())

            tvAmount.text = formatAmount(worker.amount)
            tvAshaIdCenter.text = "${worker.ashaId} · ${worker.serviceCenter}"

            when (worker.status) {
                VerificationStatus.VERIFIED -> {
                    statusBadge.text = textShort.context.getString(R.string.verified_text)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_verified)
                    tvStatusDate.text =
                        textShort.context.getString(R.string.verified_date_txt, worker.approvalDate)
                    tvRejectedReason.visibility = View.GONE
                    if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) && worker.role.equals("ASHA Supervisor")) {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${itemView.context.getString(R.string.mitanin_trainer)})"

                    } else {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${worker.role})"

                    }
                }

                VerificationStatus.PENDING -> {
                    statusBadge.text = statusBadge.context.getString(R.string.pending_txt)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatusDate.visibility = View.GONE
                    tvRejectedReason.visibility = View.GONE
                    tvStatusBy.visibility = View.GONE
                }

                VerificationStatus.REJECTED -> {
                    statusBadge.text = statusBadge.context.getString(R.string.rejected_txt)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_rejected)
                    tvRejectedReason.visibility = View.VISIBLE
                    tvStatusDate.text = tvStatusDate.context.getString(
                        R.string.rejected_date_txt,
                        HelperUtil.formatDate(worker.approvalDate)
                    )
                    tvRejectedReason.text =
                        tvRejectedReason.context.getString(R.string.reason_txt, worker.reason, worker.OtherReason)
                    if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) && worker.role.equals("ASHA Supervisor")) {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${itemView.context.getString(R.string.mitanin_trainer)})"

                    } else {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${worker.role})"


                    }

                }

                VerificationStatus.OVERDUE -> {
                    statusBadge.text = textShort.context.getString(R.string.overdue)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_rejected)
                    tvStatusDate.visibility = View.GONE
                    tvRejectedReason.visibility = View.GONE
                    tvStatusBy.visibility = View.GONE
                }

                VerificationStatus.ALL -> {
                    statusBadge.text = statusBadge.context.getString(R.string.pending_txt)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                }

                VerificationStatus.UNCLAIMED -> {
                    tvAmount.visibility = View.INVISIBLE
                    statusBadge.text = statusBadge.context.getString(R.string.unclaimed_txt)
                    statusBadge.setBackgroundResource(R.drawable.unclaimed_grey)
                }


                VerificationStatus.APPROVED -> {
                    statusBadge.text = textShort.context.getString(R.string.verified_text)
                    statusBadge.setBackgroundResource(R.drawable.bg_status_verified)
                    tvStatusDate.visibility = View.GONE
                    tvStatusDate.text = textShort.context.getString(R.string.verified_date_txt, worker.approvalDate)
                    tvRejectedReason.visibility = View.GONE
                    tvStatusBy.visibility = View.GONE
                    if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) && worker.role.equals("ASHA Supervisor")) {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${itemView.context.getString(R.string.mitanin_trainer)})"

                    } else {
                        tvStatusBy.text = "By: ${worker.verifiedByUserName}\n(${worker.role})"

                    }
                }
            }

            itemView.setOnClickListener { onItemClick(worker) }
        }

        private fun formatAmount(amount: Int): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }


    class AshaWorkerDiffCallback : DiffUtil.ItemCallback<AshaWorker>() {
        override fun areItemsTheSame(oldItem: AshaWorker, newItem: AshaWorker) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AshaWorker, newItem: AshaWorker) =
            oldItem == newItem
    }

}

