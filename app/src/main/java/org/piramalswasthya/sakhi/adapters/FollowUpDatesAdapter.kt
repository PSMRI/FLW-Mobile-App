package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemFollowUpDateBinding
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import java.text.SimpleDateFormat
import java.util.*

class FollowUpDatesAdapter :
    ListAdapter<LeprosyFollowUpCache, FollowUpDatesAdapter.FollowUpViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUpViewHolder {
        val binding = ItemFollowUpDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FollowUpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowUpViewHolder, position: Int) {
        val followUp = getItem(position)
        holder.bind(followUp)
    }

    inner class FollowUpViewHolder(private val binding: ItemFollowUpDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(followUp: LeprosyFollowUpCache) {
            binding.tvVisitNumber.text = "Visit ${followUp.visitNumber}"
            binding.tvFollowUpDate.text = dateFormat.format(Date(followUp.followUpDate))
            binding.tvTreatmentStatus.text = followUp.treatmentStatus ?: "Pending"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LeprosyFollowUpCache>() {
        override fun areItemsTheSame(oldItem: LeprosyFollowUpCache, newItem: LeprosyFollowUpCache): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LeprosyFollowUpCache, newItem: LeprosyFollowUpCache): Boolean {
            return oldItem == newItem
        }
    }
}