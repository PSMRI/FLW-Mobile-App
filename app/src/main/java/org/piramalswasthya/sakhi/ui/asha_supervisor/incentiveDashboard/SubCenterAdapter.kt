package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutListItemBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model.Facility

class SubCenterAdapter(private val onSubCenterClick: (Facility) -> Unit) : ListAdapter<Facility, SubCenterAdapter.SubCenterViewHolder>(DiffCallback()) {

    inner class SubCenterViewHolder(private val binding: LayoutListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(facility: Facility) {
            binding.subCenterName.text = facility.facilityName
            binding.tvAshaCount.text = "ASHAs: ${facility.ashaCount}"
            binding.subCenterName.setOnClickListener {
                onSubCenterClick(facility)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCenterViewHolder {
        val binding = LayoutListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubCenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubCenterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Facility>() {
        override fun areItemsTheSame(oldItem: Facility, newItem: Facility) =
            oldItem.facilityId == newItem.facilityId

        override fun areContentsTheSame(oldItem: Facility, newItem: Facility) =
            oldItem == newItem
    }
}