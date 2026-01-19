package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.adapters.FollowUpDatesAdapter
import org.piramalswasthya.sakhi.databinding.ItemFollowUpDateBinding
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TBFollowUpDatesAdapter :
    ListAdapter<TBConfirmedTreatmentCache, TBFollowUpDatesAdapter.FollowUpViewHolder>(DiffCallback) {

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

        fun bind(followUp: TBConfirmedTreatmentCache) {
            if(followUp.followUpDate!=null)
            binding.tvFollowUpDate.text = dateFormat.format(Date(followUp.followUpDate!!))
            binding.tvTreatmentStatus.visibility = View.GONE
            binding.tvLastFollowUpDate.visibility = View.VISIBLE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TBConfirmedTreatmentCache>() {
        override fun areItemsTheSame(oldItem: TBConfirmedTreatmentCache, newItem: TBConfirmedTreatmentCache): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TBConfirmedTreatmentCache, newItem: TBConfirmedTreatmentCache): Boolean {
            return oldItem == newItem
        }
    }
}