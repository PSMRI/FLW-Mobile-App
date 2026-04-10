package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemBeneficiaryBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryRecordUI

class BeneficiaryAdapter : ListAdapter<BeneficiaryRecordUI, BeneficiaryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemBeneficiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BeneficiaryRecordUI, position: Int) {
            binding.tvSerialNo.text = position.toString()
            binding.tvBenId.text = "Ben ID: ${item.benId}"
            binding.tvName.text = "Name: ${item.name ?: "-"}"
            binding.tvRchId.text = "RCH ID: ${item.rchId ?: "N/A"}"
            binding.tvAbhaNumber.text = "ABHA Number: ${item.abhaNumber ?: "N/A"}"
            binding.tvAmount.text = "₹ ${item.amount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemBeneficiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position + 1)

    class DiffCallback : DiffUtil.ItemCallback<BeneficiaryRecordUI>() {
        override fun areItemsTheSame(o: BeneficiaryRecordUI, n: BeneficiaryRecordUI) = o.id == n.id
        override fun areContentsTheSame(o: BeneficiaryRecordUI, n: BeneficiaryRecordUI) = o == n
    }
}