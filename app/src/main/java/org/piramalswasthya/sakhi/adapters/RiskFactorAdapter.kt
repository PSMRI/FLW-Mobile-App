package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemRiskFactorBinding

class RiskFactorAdapter : ListAdapter<String, RiskFactorAdapter.RiskFactorViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }

    class RiskFactorViewHolder private constructor(
        private val binding: RvItemRiskFactorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(riskFactor: String) {
            binding.riskFactor = riskFactor
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RiskFactorViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvItemRiskFactorBinding.inflate(inflater, parent, false)
                return RiskFactorViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RiskFactorViewHolder.from(parent)

    override fun onBindViewHolder(holder: RiskFactorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}