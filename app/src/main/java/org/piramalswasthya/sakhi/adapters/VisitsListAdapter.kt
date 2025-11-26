package org.piramalswasthya.sakhi.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemVisitsBinding
import org.piramalswasthya.sakhi.helpers.getDateFromLong
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import org.piramalswasthya.sakhi.model.getDateTimeStringFromLong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class VisitsListAdapter :
    ListAdapter<MalariaScreeningCache, VisitsListAdapter.FollowUpViewHolder>(DiffCallback) {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUpViewHolder {
        val binding = RvItemVisitsBinding.inflate(
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

    inner class FollowUpViewHolder(private val binding: RvItemVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(followUp: MalariaScreeningCache) {
            binding.tvVisitNumber.text = "Visit ${followUp.visitId}"
            binding.tvFollowUpDate.text = getDateStrFromLong(followUp.caseDate)
            binding.tvTreatmentStatus.text = followUp.caseStatus ?: "Suspected"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MalariaScreeningCache>() {
        override fun areItemsTheSame(oldItem: MalariaScreeningCache, newItem: MalariaScreeningCache): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MalariaScreeningCache, newItem: MalariaScreeningCache): Boolean {
            return oldItem == newItem
        }
    }
}