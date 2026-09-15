package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ItemBeneficiaryBinding
import org.piramalswasthya.sakhi.databinding.LayoutMtInnerBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryRecordUI
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Claimed records for one activity.
 *
 * Mitanin (BRD 164692128 §19.3/§19.4): each row carries its own tick and cross which act
 * immediately — no checkbox, no bulk footer. Other flavours keep the read-only beneficiary row.
 */
class BeneficiaryAdapter(
    var activityName: String,
    private val onApprove: (BeneficiaryRecordUI) -> Unit = {},
    private val onReject: (BeneficiaryRecordUI) -> Unit = {},
    private val showActions: () -> Boolean = { false }
) : ListAdapter<BeneficiaryRecordUI, BeneficiaryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BeneficiaryRecordUI, position: Int) {

            when (binding) {

                is ItemBeneficiaryBinding -> {
                    binding.tvSerialNo.text = position.toString()
                    binding.tvBenId.text = "Ben ID: ${item.benId}"
                    binding.tvName.text = "Name: ${item.name ?: "-"}"
                    binding.tvRchId.text = "RCH ID: ${item.rchId ?: "N/A"}"
                    binding.tvAbhaNumber.text = "ABHA Number: ${item.abhaNumber ?: "N/A"}"
                    binding.tvAmount.text = "₹ ${item.amount}"
                }

                is LayoutMtInnerBinding -> {
                    binding.tvSerialNo.text = position.toString()
                    binding.tvAmount.text = "₹${item.amount}"

                    // A meeting or register claim has no beneficiary behind it, so show the date
                    // it happened instead — §19.4 wants the reviewer checking something real.
                    val hasBeneficiary = item.benId > 0L
                    binding.tvBenId.visibility = visibleIf(hasBeneficiary)
                    binding.tvName.visibility = visibleIf(hasBeneficiary)
                    binding.tvRchId.visibility = visibleIf(hasBeneficiary)
                    binding.tvAbhaNumber.visibility = visibleIf(hasBeneficiary)

                    if (hasBeneficiary) {
                        binding.tvBenId.text = "Ben ID: ${item.benId}"
                        binding.tvName.text = item.name ?: "-"
                        binding.tvRchId.text = "RCH ID: ${item.rchId ?: "N/A"}"
                        binding.tvAbhaNumber.text = "ABHA Number: ${item.abhaNumber ?: "N/A"}"
                        binding.tvDate.visibility = View.GONE
                    } else {
                        val date = formatClaimDate(item.startDate)
                        binding.tvDate.text = if (date.isBlank()) "" else
                            binding.root.context.getString(R.string.claim_date, date)
                        binding.tvDate.visibility = visibleIf(date.isNotBlank())
                    }

                    val actionable = showActions()
                    binding.btnApprove.visibility = visibleIf(actionable)
                    binding.btnReject.visibility = visibleIf(actionable)
                    if (actionable) {
                        binding.btnApprove.setOnClickListener { onApprove(item) }
                        binding.btnReject.setOnClickListener { onReject(item) }
                    } else {
                        // Clear on the recycled holder, or a read-only row keeps a live listener.
                        binding.btnApprove.setOnClickListener(null)
                        binding.btnReject.setOnClickListener(null)
                        binding.btnApprove.isClickable = false
                        binding.btnReject.isClickable = false
                    }

                    binding.rowDivider.visibility =
                        visibleIf(position < itemCount)
                }
            }
        }

        private fun visibleIf(condition: Boolean) = if (condition) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TYPE_DEFAULT = 0
        private const val TYPE_MITANIN = 1

        /**
         * `startDate` arrives either as a full ISO timestamp or as a plain `yyyy-MM-dd`, so try
         * both. Falls back to the raw value rather than blanking a date the reviewer needs.
         */
        internal fun formatClaimDate(raw: String?): String {
            if (raw.isNullOrBlank()) return ""
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
            )
            val out = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            for (pattern in patterns) {
                try {
                    val parsed = SimpleDateFormat(pattern, Locale.ENGLISH).parse(raw)
                    if (parsed != null) return out.format(parsed)
                } catch (_: Exception) {
                    // try the next pattern
                }
            }
            return raw
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (viewType) {

            TYPE_MITANIN -> {
                ViewHolder(
                    LayoutMtInnerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                ViewHolder(
                    ItemBeneficiaryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position + 1)

    class DiffCallback : DiffUtil.ItemCallback<BeneficiaryRecordUI>() {
        override fun areItemsTheSame(o: BeneficiaryRecordUI, n: BeneficiaryRecordUI) = o.id == n.id
        override fun areContentsTheSame(o: BeneficiaryRecordUI, n: BeneficiaryRecordUI) = o == n
    }

    override fun getItemViewType(position: Int): Int {
        return if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            TYPE_MITANIN
        } else {
            TYPE_DEFAULT
        }
    }
}
