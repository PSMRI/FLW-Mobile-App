package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.databinding.ItemBeneficiaryBinding
import org.piramalswasthya.sakhi.databinding.LayoutMtInnerBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryRecordUI

class BeneficiaryAdapter(var activityName: String) : ListAdapter<BeneficiaryRecordUI, BeneficiaryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ViewBinding
    ) :
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
                    binding.tvBenId.text = "Ben ID: ${item.benId}"
                    binding.tvName.text = "Name: ${item.name ?: "-"}"
                    binding.tvRchId.text = "RCH ID: ${item.rchId ?: "N/A"}"
                    binding.tvAbhaNumber.text = "ABHA Number: ${item.abhaNumber ?: "N/A"}"
                    binding.tvAmount.text = "₹ ${item.amount}"
                }
            }
        }



    }
    companion object {
        private const val TYPE_DEFAULT = 0
        private const val TYPE_MITANIN = 1
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