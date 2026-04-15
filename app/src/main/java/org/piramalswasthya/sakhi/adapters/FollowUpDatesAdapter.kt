package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ItemFollowUpDateBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.*

class FollowUpDatesAdapter :
    ListAdapter<LeprosyFollowUpCache, FollowUpDatesAdapter.FollowUpViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

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
            binding.tvFollowUpDate.text = dateFormat.format(Date(followUp.followUpDate))
            val ctx = binding.root.context
            val englishArray = HelperUtil.getLocalizedResources(ctx, Languages.ENGLISH)
                .getStringArray(R.array.leprosy_treatment_status_before_time)
            val localizedArray = ctx.resources.getStringArray(R.array.leprosy_treatment_status_before_time)
            val idx = englishArray.indexOf(followUp.treatmentStatus)
            binding.tvTreatmentStatus.text = if (idx >= 0) localizedArray[idx]
            else ctx.getString(R.string.pending)
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